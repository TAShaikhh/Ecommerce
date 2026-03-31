package com.forwardauction.iam.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.forwardauction.iam.dto.LoginRequest;
import com.forwardauction.iam.dto.ResetPasswordRequest;
import com.forwardauction.iam.dto.SignupRequest;

@SpringBootTest
class AuthServiceExtendedTest {

    @Autowired
    private AuthService authService;

    @Test
    void resetPassword_thenLoginWithNewPassword() {
        authService.signup(new SignupRequest("resetuser", "oldpass", "A", "B", "Addr"));
        authService.resetPassword(new ResetPasswordRequest("resetuser", "oldpass", "newpass"));
        var res = authService.login(new LoginRequest("resetuser", "newpass"));
        assertEquals("resetuser", res.username());
    }

    @Test
    void resetPassword_unknownUser_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("ghost", "oldpass", "newpass")));
    }

    @Test
    void resetPassword_wrongCurrentPassword_throws() {
        authService.signup(new SignupRequest("resetuser2", "oldpass", "A", "B", "Addr"));
        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("resetuser2", "badpass", "newpass")));
    }

    @Test
    void getProfileById_returnsAddress() {
        var signup = authService.signup(new SignupRequest("addruser", "pass", "First", "Last", "123 Main St"));
        var profile = authService.getProfileById(signup.userId());
        assertEquals("123 Main St", profile.address());
        assertEquals("First", profile.firstName());
    }

    @Test
    void getProfileByUsername_returnsProfile() {
        authService.signup(new SignupRequest("profuser", "pass", "F", "L", "456 Oak Ave"));
        var profile = authService.getProfileByUsername("profuser");
        assertEquals("profuser", profile.username());
        assertEquals("456 Oak Ave", profile.address());
    }
}
