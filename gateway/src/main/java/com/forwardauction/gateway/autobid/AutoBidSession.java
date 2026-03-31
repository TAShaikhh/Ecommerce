package com.forwardauction.gateway.autobid;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an active AI auto-bid session.
 * Tracks configuration, current state, and a timestamped action log.
 */
public class AutoBidSession {

    private final String sessionId;
    private final String token;
    private final String username;
    private final long itemId;
    private final int maxBid;
    private final String strategy;
    private volatile String status; // ACTIVE, STOPPED, WON, OUTBID_MAX, AUCTION_ENDED
    private volatile int bidsPlaced;
    private volatile int currentBid;
    private final List<ActionEntry> actionLog;

    public AutoBidSession(String sessionId, String token, String username,
                          long itemId, int maxBid, String strategy) {
        this.sessionId = sessionId;
        this.token = token;
        this.username = username;
        this.itemId = itemId;
        this.maxBid = maxBid;
        this.strategy = strategy;
        this.status = "ACTIVE";
        this.bidsPlaced = 0;
        this.currentBid = 0;
        this.actionLog = Collections.synchronizedList(new ArrayList<>());
        addAction("SESSION_STARTED", 0, "Strategy: " + strategy + ", Max budget: $" + maxBid);
    }

    public void addAction(String action, int amount, String details) {
        actionLog.add(new ActionEntry(Instant.now().toString(), action, amount, details));
    }

    // --- Getters ---
    public String getSessionId() { return sessionId; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public long getItemId() { return itemId; }
    public int getMaxBid() { return maxBid; }
    public String getStrategy() { return strategy; }
    public String getStatus() { return status; }
    public int getBidsPlaced() { return bidsPlaced; }
    public int getCurrentBid() { return currentBid; }
    public List<ActionEntry> getActionLog() { return List.copyOf(actionLog); }

    // --- Setters ---
    public void setStatus(String status) { this.status = status; }
    public void setCurrentBid(int currentBid) { this.currentBid = currentBid; }
    public void incrementBidsPlaced() { this.bidsPlaced++; }

    /** Timestamped entry in the auto-bid action log. */
    public record ActionEntry(String timestamp, String action, int amount, String details) {}
}
