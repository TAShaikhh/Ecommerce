package com.forwardauction.gateway;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthProxyHelperTest {

    @Test
    void extractToken_validBearerHeader_returnsToken() {
        assertEquals("abc123", AuthProxyController.extractToken("Bearer abc123"));
    }

    @Test
    void extractToken_nullHeader_throwsSecurityException() {
        assertThrows(SecurityException.class, () -> AuthProxyController.extractToken(null));
    }

    @Test
    void extractToken_missingBearerPrefix_throwsSecurityException() {
        assertThrows(SecurityException.class, () -> AuthProxyController.extractToken("Basic abc123"));
    }

    @Test
    void extractToken_emptyHeader_throwsSecurityException() {
        assertThrows(SecurityException.class, () -> AuthProxyController.extractToken(""));
    }

    @Test
    void extractToken_bearerOnly_throwsSecurityException() {
        SecurityException ex = assertThrows(SecurityException.class, () -> AuthProxyController.extractToken("Bearer "));
        assertEquals("Missing bearer token", ex.getMessage());
    }

    @Test
    void extractToken_placeholder_throwsHelpfulSecurityException() {
        SecurityException ex = assertThrows(SecurityException.class, () -> AuthProxyController.extractToken("Bearer <token>"));
        assertEquals("Replace the <token> placeholder with the token returned by POST /auth/login", ex.getMessage());
    }
}
