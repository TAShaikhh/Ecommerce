package com.forwardauction.gateway.autobid;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Server-side scheduler that ticks every 3 seconds and evaluates
 * all active auto-bid sessions against live auction state.
 * Mirrors the AuctionScheduler pattern in the Auction service.
 */
@Component
public class AutoBidScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoBidScheduler.class);

    private final AutoBidStore store;
    private final RestTemplate restTemplate;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    // Strategy instances (stateless — safe to share)
    private final ConservativeBidStrategy conservative = new ConservativeBidStrategy();
    private final AggressiveBidStrategy aggressive = new AggressiveBidStrategy();
    private final SniperBidStrategy sniper = new SniperBidStrategy();

    public AutoBidScheduler(AutoBidStore store, RestTemplate restTemplate) {
        this.store = store;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 3000)
    public void tick() {
        for (AutoBidSession session : store.findActive()) {
            try {
                processSession(session);
            } catch (Exception e) {
                log.warn("Auto-bid tick failed for session {}: {}", session.getSessionId(), e.getMessage());
                session.addAction("ERROR", 0, e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processSession(AutoBidSession session) {
        // 1. Fetch live auction state
        Map<String, Object> raw = restTemplate.getForObject(
                auctionBaseUrl + "/auctions/by-item/" + session.getItemId(), Map.class);

        if (raw == null) {
            session.setStatus("AUCTION_ENDED");
            session.addAction("ENDED", 0, "Could not fetch auction state.");
            return;
        }

        String status = (String) raw.get("status");
        String result = (String) raw.get("result");
        double currentHighestBid = raw.get("currentHighestBid") != null
                ? ((Number) raw.get("currentHighestBid")).doubleValue() : 0;
        String highestBidder = (String) raw.get("highestBidderUsername");
        long auctionId = ((Number) raw.get("auctionId")).longValue();
        long remainingSeconds = raw.get("remainingSeconds") != null
                ? ((Number) raw.get("remainingSeconds")).longValue() : 0;

        BidStrategy.AuctionState auctionState = new BidStrategy.AuctionState(
                auctionId, session.getItemId(), status, result,
                currentHighestBid, highestBidder, remainingSeconds
        );

        // 2. Handle ended auctions
        if ("ENDED".equals(status)) {
            boolean won = session.getUsername().equals(highestBidder);
            session.setStatus(won ? "WON" : "AUCTION_ENDED");
            session.addAction(won ? "WON" : "AUCTION_ENDED", (int) currentHighestBid,
                    won ? "You won the auction!" : "Auction ended. Winner: " + highestBidder);
            return;
        }

        // 3. Evaluate the strategy
        BidStrategy strategy = resolveStrategy(session.getStrategy());
        int bidAmount = strategy.evaluate(session, auctionState);

        if (bidAmount == 0) {
            // Strategy says don't bid this tick (we're winning, or sniper waiting)
            return;
        }

        // 4. Check budget
        if (bidAmount > session.getMaxBid()) {
            session.setStatus("OUTBID_MAX");
            session.addAction("MAX_EXCEEDED", bidAmount,
                    "Required bid $" + bidAmount + " exceeds max budget $" + session.getMaxBid());
            return;
        }

        // 5. Place the bid via the Auction service
        try {
            Map<String, Object> bidBody = Map.of(
                    "bidderUsername", session.getUsername(),
                    "amount", bidAmount
            );

            restTemplate.postForObject(
                    auctionBaseUrl + "/auctions/" + auctionId + "/bids",
                    bidBody, Map.class);

            session.incrementBidsPlaced();
            session.setCurrentBid(bidAmount);
            session.addAction("BID_PLACED", bidAmount,
                    strategyReason(session.getStrategy(), (int) currentHighestBid, bidAmount));

            log.info("Auto-bid: {} placed ${} on item {} (strategy: {})",
                    session.getUsername(), bidAmount, session.getItemId(), session.getStrategy());

        } catch (Exception e) {
            // Bid rejection (concurrent update, auction ended mid-tick, etc.)
            session.addAction("BID_REJECTED", bidAmount, e.getMessage());
            log.debug("Auto-bid rejected for {}: {}", session.getSessionId(), e.getMessage());
        }
    }

    private BidStrategy resolveStrategy(String name) {
        return switch (name) {
            case "AGGRESSIVE" -> aggressive;
            case "SNIPER" -> sniper;
            default -> conservative;
        };
    }

    private String strategyReason(String strategy, int previousBid, int newBid) {
        return switch (strategy) {
            case "AGGRESSIVE" -> "Outbid detected ($" + previousBid + "). Bid 10% above.";
            case "SNIPER" -> "Snipe window active. Placed bid.";
            default -> "Outbid detected ($" + previousBid + "). Bid +$1 increment.";
        };
    }
}
