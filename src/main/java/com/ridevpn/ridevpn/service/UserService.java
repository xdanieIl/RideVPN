package com.ridevpn.ridevpn.service;

import com.ridevpn.ridevpn.model.User;
import com.ridevpn.ridevpn.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
