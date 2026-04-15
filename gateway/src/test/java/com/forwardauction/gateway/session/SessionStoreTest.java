package com.forwardauction.gateway.session;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionStoreTest {

    private SessionStore store;

    @BeforeEach
    void setUp() {
        store = new SessionStore();
    }

    @Test
    void createSession_returnsToken() {
        String token = store.createSession(1L, "alice");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getSession_validToken_returnsData() {
        String token = store.createSession(1L, "alice");
        var session = store.getSession(token);
        assertTrue(session.isPresent());
        assertEquals(1L, session.get().userId());
        assertEquals("alice", session.get().username());
        assertNull(session.get().selectedItemId());
    }

    @Test
    void getSession_invalidToken_returnsEmpty() {
        var session = store.getSession("nonexistent-token");
        assertTrue(session.isEmpty());
    }

    @Test
    void getSession_nullToken_returnsEmpty() {
        assertTrue(store.getSession(null).isEmpty());
    }

    @Test
    void getSession_blankToken_returnsEmpty() {
        assertTrue(store.getSession("  ").isEmpty());
    }

    @Test
    void invalidate_removesSession() {
        String token = store.createSession(1L, "alice");
        store.invalidate(token);
        assertTrue(store.getSession(token).isEmpty());
    }

    @Test
    void selectItem_setsItemId() {
        String token = store.createSession(1L, "alice");
        store.selectItem(token, 42L);
        var session = store.getSession(token);
        assertTrue(session.isPresent());
        assertEquals(42L, session.get().selectedItemId());
    }

    @Test
    void selectItem_replacesExistingSelection() {
        String token = store.createSession(1L, "alice");
        store.selectItem(token, 42L);
        store.selectItem(token, 99L);
        assertEquals(99L, store.getSession(token).get().selectedItemId());
    }

    @Test
    void selectItem_invalidToken_throwsSecurityException() {
        assertThrows(SecurityException.class, () -> store.selectItem("bad-token", 1L));
    }

    @Test
    void multipleSessions_areIndependent() {
        String t1 = store.createSession(1L, "alice");
        String t2 = store.createSession(2L, "bob");
        assertNotEquals(t1, t2);
        assertEquals("alice", store.getSession(t1).get().username());
        assertEquals("bob", store.getSession(t2).get().username());
    }
}
