package com.ridevpn.ridevpn.controller;

import com.ridevpn.ridevpn.model.User;
import com.ridevpn.ridevpn.repository.UserRepository;
import com.ridevpn.ridevpn.service.WireGuardService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final WireGuardService wireGuardService;
    private final UserRepository userRepository;

    public ApiController(WireGuardService wireGuardService, UserRepository userRepository) {
        this.wireGuardService = wireGuardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public String me(Principal principal) {
        return "Hello " + principal.getName();
    }

    // Downloads config file for the client to use
    @PostMapping(value = "/config", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> config(Principal principal) {
        try {
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"ridevpn.conf\"")
                .header("Cache-Control", "no-store")
                .body(wireGuardService.createConfig(principal.getName()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return configUnavailable();
        } catch (java.io.IOException | IllegalStateException e) {
            return configUnavailable();
        }
    }

    private ResponseEntity<String> configUnavailable() {
        return ResponseEntity.status(503).header("Cache-Control", "no-store")
            .body("Could not generate your VPN config. Please try again later.");
    }

    // The dashboard hits this endpoint to get connection status of the logged in user
    @GetMapping("/status")
    public Map<String, Object> status(Principal principal) throws Exception {
        boolean connected = wireGuardService.isConnected(principal.getName());
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        long sessionSeconds = connected
                ? Duration.between(user.getConnectedSince(), Instant.now()).getSeconds()
                : 0;
        return Map.of(
                "connected", connected,
                "ip", user.getVpnIp() + "",
                "sessionSeconds", sessionSeconds);
    }
}
