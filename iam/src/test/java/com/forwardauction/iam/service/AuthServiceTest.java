package com.forwardauction.iam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.forwardauction.iam.dto.LoginRequest;
import com.forwardauction.iam.dto.SignupRequest;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void signup_createsUser_andLoginSucceeds() {
        var signup = new SignupRequest(
                "testuser1",
                "pass1234",
                "Test",
                "User",
                "123 Test St"
        );

        var signupRes = authService.signup(signup);
        assertTrue(signupRes.userId() > 0);
        assertEquals("testuser1", signupRes.username());

        var loginRes = authService.login(new LoginRequest("testuser1", "pass1234"));
        assertEquals(signupRes.userId(), loginRes.userId());
        assertEquals("testuser1", loginRes.username());
    }

    @Test
    void signup_duplicateUsername_throws() {
        var signup = new SignupRequest(
                "dupe",
                "pass1234",
                "A",
                "B",
                "Addr"
        );

        authService.signup(signup);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(signup)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void login_wrongPassword_throws() {
        authService.signup(new SignupRequest(
                "pwtest",
                "correct",
                "A",
                "B",
                "Addr"
        ));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(new LoginRequest("pwtest", "wrong"))
        );

        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    void login_unknownUser_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(new LoginRequest("no_such_user", "pass1234"))
        );

        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }
}