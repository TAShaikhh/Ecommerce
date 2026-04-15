package com.forwardauction.iam.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.forwardauction.iam.dto.AuthResponse;
import com.forwardauction.iam.dto.LoginRequest;
import com.forwardauction.iam.dto.ResetPasswordRequest;
import com.forwardauction.iam.dto.SignupRequest;
import com.forwardauction.iam.dto.UserProfileResponse;
import com.forwardauction.iam.service.AuthService;

import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.status(201).body(auth.signup(req));
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/auth/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        auth.resetPassword(req);
        return Map.of("status", "OK", "message", "Password updated successfully");
    }

    @GetMapping("/users/{userId}/address")
    public UserProfileResponse getUserAddress(@PathVariable long userId) {
        return auth.getProfileById(userId);
    }

    @GetMapping("/users/by-username/{username}")
    public UserProfileResponse getUserByUsername(@PathVariable String username) {
        return auth.getProfileByUsername(username);
    }
}
