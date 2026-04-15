package com.forwardauction.iam.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.forwardauction.iam.dto.AuthResponse;
import com.forwardauction.iam.dto.LoginRequest;
import com.forwardauction.iam.dto.ResetPasswordRequest;
import com.forwardauction.iam.dto.SignupRequest;
import com.forwardauction.iam.dto.UserProfileResponse;
import com.forwardauction.iam.repo.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public AuthResponse signup(SignupRequest req) {
        String hash = encoder.encode(req.password());
        long userId = users.createUser(req.username(), hash, req.firstName(), req.lastName(), req.address());
        return new AuthResponse(userId, req.username());
    }

    public AuthResponse login(LoginRequest req) {
        var user = users.findByUsername(req.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!encoder.matches(req.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return new AuthResponse(user.id(), user.username());
    }

    public void resetPassword(ResetPasswordRequest req) {
        var user = users.findByUsername(req.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!encoder.matches(req.currentPassword(), user.passwordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        String hash = encoder.encode(req.newPassword());
        users.updatePassword(req.username(), hash);
    }

    public UserProfileResponse getProfileById(long userId) {
        var profile = users.findProfileById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserProfileResponse(profile.id(), profile.username(),
                profile.firstName(), profile.lastName(), profile.address());
    }

    public UserProfileResponse getProfileByUsername(String username) {
        var profile = users.findProfileByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserProfileResponse(profile.id(), profile.username(),
                profile.firstName(), profile.lastName(), profile.address());
    }
}
