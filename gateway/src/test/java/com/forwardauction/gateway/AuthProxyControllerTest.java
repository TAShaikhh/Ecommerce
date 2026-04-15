package com.forwardauction.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.session.SessionStore;

class AuthProxyControllerTest {

    private RestTemplate restTemplate;
    private SessionStore sessionStore;
    private AuthProxyController controller;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        sessionStore = mock(SessionStore.class);
        controller = new AuthProxyController(restTemplate, sessionStore);
        ReflectionTestUtils.setField(controller, "iamBaseUrl", "http://iam");

        when(sessionStore.getSession("token"))
                .thenReturn(Optional.of(new SessionStore.SessionData(1L, "alice", null, 0L)));
    }

    @Test
    void resetPassword_usernameMismatch_forbidden() {
        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> controller.resetPassword(Map.of("username", "bob", "currentPassword", "x", "newPassword", "y"), "Bearer token")
        );
        assertEquals("You can only reset your own password", ex.getMessage());
        verifyNoInteractions(restTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void resetPassword_usernameMatches_proxiesAndAddsLinks() {
        when(restTemplate.postForObject(eq("http://iam/auth/reset-password"), any(), eq(Map.class)))
                .thenReturn(Map.of("status", "OK", "message", "Password updated successfully"));

        Map<String, Object> res = controller.resetPassword(
                Map.of("username", "alice", "currentPassword", "oldpass", "newPassword", "newpass"),
                "Bearer token"
        );

        assertEquals("OK", res.get("status"));
        Map<String, Object> links = (Map<String, Object>) res.get("_links");
        assertEquals("/auth/reset-password", ((Map<?, ?>) links.get("self")).get("href"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void logout_includesLinks() {
        Map<String, Object> res = controller.logout("Bearer token");

        verify(sessionStore).invalidate("token");
        assertEquals("OK", res.get("status"));
        Map<String, Object> links = (Map<String, Object>) res.get("_links");
        assertEquals("/auth/logout", ((Map<?, ?>) links.get("self")).get("href"));
        assertEquals("/auth/login", ((Map<?, ?>) links.get("login")).get("href"));
    }
}
