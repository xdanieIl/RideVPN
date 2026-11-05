package com.ridevpn.ridevpn.service;

import com.ridevpn.ridevpn.model.User;
import com.ridevpn.ridevpn.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public void register(String username, String email, String password) {
        User user = new User(username, email, passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public boolean checkLogin(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(password, user.get().getPasswordHash());
    }


    public void updateEmail(String username, String email) {
        String value = email.trim();
        if (value.length() > 254 || !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setEmail(value);
        userRepository.saveAndFlush(user);
    }

    public void updatePassword(String username, String currentPassword, String newPassword, String confirmation) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (!newPassword.equals(confirmation)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }
        if (newPassword.length() < 12 || newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Use at least 12 characters and at most 72 UTF-8 bytes for your password.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
