package com.smartbuilding.service;

import com.smartbuilding.model.Role;
import com.smartbuilding.model.User;
import com.smartbuilding.repository.UserRepository;
import com.smartbuilding.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Business logic for login / register.
// The controller only passes input along; the real work happens here.
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // safely checks hashed passwords
    private final JwtUtil jwtUtil;                 // creates tokens

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Login: check username + password, return tokens if correct.
    public Map<String, Object> login(String username, String password) {
        // 1. Does this user exist?
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty()) {
            return Map.of("error", "Invalid credentials");
        }

        // 2. Does the typed password match the stored (hashed) one?
        //    Never compare passwords with equals() — always use matches().
        User user = found.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Map.of("error", "Invalid credentials");
        }

        // 3. Correct! Create tokens and send them back.
        return tokensFor(user);
    }

    // Register: create a new USER account, then log them in immediately.
    public Map<String, Object> register(String username, String email, String password,
                                        String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            return Map.of("error", "Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            return Map.of("error", "Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // NEVER store plain passwords
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.USER); // new accounts are always normal users
        user.setEnabled(true);
        userRepository.save(user);

        return tokensFor(user);
    }

    // Refresh: the access token expired, but the refresh token is still good?
    // Then hand out a fresh pair of tokens without asking for the password again.
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return Map.of("error", "Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty()) {
            return Map.of("error", "User not found");
        }

        return tokensFor(found.get());
    }

    // Shared helper: builds the "access token + refresh token + user info" answer.
    // Used by login, register and refresh so the code exists only once.
    private Map<String, Object> tokensFor(User user) {
        // Extra info stored INSIDE the token (so we don't query the DB on every request).
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        Map<String, Object> answer = new HashMap<>();
        answer.put("accessToken", jwtUtil.generateToken(user.getUsername(), claims));
        answer.put("refreshToken", jwtUtil.generateRefreshToken(user.getUsername(), claims));
        answer.put("tokenType", "Bearer");
        answer.put("username", user.getUsername());
        answer.put("role", user.getRole().name());
        answer.put("userId", user.getId());
        return answer;
    }
}