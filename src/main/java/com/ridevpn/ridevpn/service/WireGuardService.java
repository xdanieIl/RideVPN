package com.ridevpn.ridevpn.service;

import com.ridevpn.ridevpn.model.User;
import com.ridevpn.ridevpn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
public class WireGuardService {

    private final UserRepository userRepository;
    private final String serverPublicKey; // from WG_SERVER_PUBLIC_KEY in the env file
    private final String serverPublicIp;  // from WG_ENDPOINT in the env file

    public WireGuardService(UserRepository userRepository,
                            @Value("${wireguard.server-public-key}") String serverPublicKey,
                            @Value("${wireguard.endpoint}") String serverPublicIp) {
        this.userRepository = userRepository;
        this.serverPublicKey = serverPublicKey;
        this.serverPublicIp = serverPublicIp;
    }

    // Makes a new VPN config for the user and returns it as text
    public synchronized String createConfig(String username) throws IOException, InterruptedException {
        User user = userRepository.findByUsername(username).orElseThrow();
        String ip = getIpForUser(user);

        // Check if the user had an old key, remove it from the server
        if (user.getWgPublicKey() != null) {
            removePeer(user.getWgPublicKey());
        }

        // Make a new key pair for the user
        String privateKey = generatePrivateKey();
        String publicKey = getPublicKey(privateKey);

        // Add the new key to wireguard
        addPeer(publicKey, ip);

        // Save the public key that was used to the users db entry
        user.setWgPublicKey(publicKey);
        userRepository.save(user);

        // Send back the config file
        return buildConfigFile(privateKey, ip);
    }

    // Returns the user's IP. If they don't have one yet, gives them the first free one.
    // 10.200.0.1 is the server, so users get 10.200.0.2 to 10.200.0.254
    private String getIpForUser(User user) {
        if (user.getVpnIp() != null) {
            return user.getVpnIp();
        }

        List<String> usedIps = userRepository.findAllVpnIps();
        for (int i = 2; i < 255; i++) { // start at 2 because 10.200.0.0 is the network address, and 10.200.0.1 is the server address.
            String ip = "10.200.0." + i;
            if (!usedIps.contains(ip)) {
                user.setVpnIp(ip);
                return ip;
            }
        }
        throw new IllegalStateException("No IPs left to assign");
    }

    private String generatePrivateKey() throws IOException, InterruptedException {
        return runCommand(null, "wg", "genkey");
    }

    private String getPublicKey(String privateKey) throws IOException, InterruptedException {
        return runCommand(privateKey, "wg", "pubkey");
    }

    private void addPeer(String publicKey, String ip) throws IOException, InterruptedException {
        runCommand(null, "sudo", "-n", "wg", "set", "wg0", "peer", publicKey, "allowed-ips", ip + "/32");
    }

    private void removePeer(String publicKey) throws IOException, InterruptedException {
        runCommand(null, "sudo", "-n", "wg", "set", "wg0", "peer", publicKey, "remove");
    }

    private String buildConfigFile(String privateKey, String ip) {
        return """
                [Interface]
                PrivateKey = %s
                Address = %s/32
                DNS = 1.1.1.1

                [Peer]
                PublicKey = %s
                Endpoint = %s
                AllowedIPs = 0.0.0.0/0
                PersistentKeepalive = 25
                """.formatted(privateKey, ip, serverPublicKey, serverPublicIp);
    }

    // Runs a terminal command and returns what it prints.
    // input is typed into the command (use null if there is none).
    private String runCommand(String input, String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

        if (input != null) {
            process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
        }
        process.getOutputStream().close();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        if (process.waitFor() != 0) {
            throw new IOException("Command failed: " + String.join(" ", command) + " -> " + output);
        }
        return output;
    }

    // Returns true if the user's client talked to the server in the last 3 minutes.
    // Also saves when the session started (connected_since) and clears it on disconnect.
    // synchronized so it can't save over a new key that createConfig is saving at the same time.
    public synchronized boolean isConnected(String username) throws IOException, InterruptedException {
        User user = userRepository.findByUsername(username).orElseThrow();
        boolean connected = false;

        String dump = runCommand(null, "sudo", "-n", "wg", "show", "wg0", "latest-handshakes");
        for (String line : dump.split("\n")) {
            String[] cols = line.split("\t"); // public key, last handshake time
            if (cols[0].equals(user.getWgPublicKey())) {
                connected = System.currentTimeMillis() / 1000 - Long.parseLong(cols[1]) < 180;
            }
        }

        if (connected && user.getConnectedSince() == null) {
            user.setConnectedSince(Instant.now());
            userRepository.save(user);
        } else if (!connected && user.getConnectedSince() != null) {
            user.setConnectedSince(null);
            userRepository.save(user);
        }
        return connected;
    }
}
