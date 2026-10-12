package com.smartbuilding.controller;

import com.smartbuilding.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Handles /api/auth/login, /api/auth/register, /api/auth/refresh.
// These three endpoints are PUBLIC (see the filter) — everything else needs a token.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login   body: { "username": "...", "password": "..." }
    // Good credentials -> 200 + tokens. Bad credentials -> 401.
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
        }

        Map<String, Object> result = authService.login(username, password);
        if (result.containsKey("error")) {
            return ResponseEntity.status(401).body(result); // 401 = Unauthorized
        }
        return ResponseEntity.ok(result);
    }

    // POST /api/auth/register   body: { "username": "...", "email": "...", "password": "..." }
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username, email, and password required"));
        }

        Map<String, Object> result = authService.register(
                username, email, password,
                request.get("firstName"), request.get("lastName"));

        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // POST /api/auth/refresh   body: { "refreshToken": "..." }
    // Gives new tokens when the old access token expired.
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token required"));
        }

        Map<String, Object> result = authService.refreshToken(refreshToken);
        if (result.containsKey("error")) {
            return ResponseEntity.status(401).body(result);
        }
        return ResponseEntity.ok(result);
    }
}