package com.forwardauction.gateway.session;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SessionStore {

    private final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();

    public String createSession(long userId, String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(userId, username, null, System.currentTimeMillis()));
        return token;
    }

    public Optional<SessionData> getSession(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return Optional.ofNullable(sessions.get(token));
    }

    public void invalidate(String token) {
        sessions.remove(token);
    }

    public void selectItem(String token, long itemId) {
        SessionData existing = sessions.get(token);
        if (existing == null) throw new SecurityException("Invalid session");
        sessions.put(token, new SessionData(existing.userId(), existing.username(), itemId, existing.createdAt()));
    }

    public record SessionData(long userId, String username, Long selectedItemId, long createdAt) {}
}
