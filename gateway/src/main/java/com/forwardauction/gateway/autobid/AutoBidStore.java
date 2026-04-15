package com.forwardauction.gateway.autobid;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * In-memory store for auto-bid sessions.
 * Follows the same ConcurrentHashMap pattern as SessionStore.
 */
@Component
public class AutoBidStore {

    private final ConcurrentHashMap<String, AutoBidSession> sessions = new ConcurrentHashMap<>();

    public void save(AutoBidSession session) {
        sessions.put(session.getSessionId(), session);
    }

    public Optional<AutoBidSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /** Find all sessions that are currently ACTIVE (for the scheduler). */
    public List<AutoBidSession> findActive() {
        return sessions.values().stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    /** Check if a user already has an active session for a given item. */
    public Optional<AutoBidSession> findActiveByUserAndItem(String username, long itemId) {
        return sessions.values().stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .filter(s -> s.getUsername().equals(username))
                .filter(s -> s.getItemId() == itemId)
                .findFirst();
    }
}
