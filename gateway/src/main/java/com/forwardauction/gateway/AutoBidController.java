package com.forwardauction.gateway;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.autobid.AutoBidSession;
import com.forwardauction.gateway.autobid.AutoBidStore;
import com.forwardauction.gateway.autobid.GenAiAdvisor;
import com.forwardauction.gateway.session.SessionStore;
import com.forwardauction.gateway.util.HateoasHelper;

/**
 * UC8: AI Auto-Bidder REST controller.
 * Provides endpoints to start, monitor, stop autonomous bidding sessions,
 * and a Gen AI chat assistant for auction advice.
 */
@RestController
public class AutoBidController {

    private final RestTemplate restTemplate;
    private final SessionStore sessionStore;
    private final AutoBidStore autoBidStore;
    private final GenAiAdvisor genAiAdvisor;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    public AutoBidController(RestTemplate restTemplate, SessionStore sessionStore,
                             AutoBidStore autoBidStore, GenAiAdvisor genAiAdvisor) {
        this.restTemplate = restTemplate;
        this.sessionStore = sessionStore;
        this.autoBidStore = autoBidStore;
        this.genAiAdvisor = genAiAdvisor;
    }

    /**
     * POST /auto-bid/start — Start an AI auto-bid session.
     * Body: { itemId, maxBid, strategy }
     */
    @PostMapping("/auto-bid/start")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> startAutoBid(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        // --- Validate request ---
        long itemId = requireLong(body, "itemId");
        int maxBid = requireInt(body, "maxBid");
        String strategy = requireString(body, "strategy");

        // Validate strategy name
        if (!strategy.equals("CONSERVATIVE") && !strategy.equals("AGGRESSIVE") && !strategy.equals("SNIPER")) {
            throw new IllegalArgumentException("strategy must be one of: CONSERVATIVE, AGGRESSIVE, SNIPER");
        }

        if (maxBid <= 0) {
            throw new IllegalArgumentException("maxBid must be positive");
        }

        // Prevent duplicate active sessions for same user + item
        autoBidStore.findActiveByUserAndItem(session.username(), itemId).ifPresent(existing -> {
            throw new IllegalArgumentException(
                    "You already have an active auto-bid session for this item (session: " + existing.getSessionId() + ")");
        });

        // Verify auction is active
        Map<String, Object> auction = restTemplate.getForObject(
                auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);
        if (auction == null || !"ACTIVE".equals(auction.get("status"))) {
            throw new IllegalArgumentException("Auction is not active for this item");
        }

        // Verify caller is not the seller
        String sellerUsername = (String) auction.get("sellerUsername");
        if (session.username().equals(sellerUsername)) {
            throw new IllegalArgumentException("Seller cannot activate auto-bidder on their own item");
        }

        // Verify maxBid > current highest
        double currentHighest = auction.get("currentHighestBid") != null
                ? ((Number) auction.get("currentHighestBid")).doubleValue() : 0;
        double startingPrice = auction.get("startingPrice") != null
                ? ((Number) auction.get("startingPrice")).doubleValue() : 0;
        if (maxBid <= Math.max(currentHighest, startingPrice)) {
            throw new IllegalArgumentException("maxBid must be higher than current price: $" + (int) Math.max(currentHighest, startingPrice));
        }

        // --- Create and save session ---
        String sessionId = "ab-" + UUID.randomUUID().toString().substring(0, 8);
        AutoBidSession abSession = new AutoBidSession(
                sessionId, token, session.username(), itemId, maxBid, strategy);
        autoBidStore.save(abSession);

        return ResponseEntity.status(201).body(buildResponse(abSession));
    }

    /**
     * GET /auto-bid/status/{sessionId} — Get auto-bid session status.
     */
    @GetMapping("/auto-bid/status/{sessionId}")
    public Map<String, Object> getAutoBidStatus(
            @PathVariable String sessionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        AuthProxyController.extractToken(authHeader); // validate auth

        AutoBidSession abSession = autoBidStore.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Auto-bid session not found: " + sessionId));

        return buildResponse(abSession);
    }

    /**
     * POST /auto-bid/stop/{sessionId} — Stop an active auto-bid session.
     */
    @PostMapping("/auto-bid/stop/{sessionId}")
    public Map<String, Object> stopAutoBid(
            @PathVariable String sessionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        AutoBidSession abSession = autoBidStore.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Auto-bid session not found: " + sessionId));

        if (!abSession.getUsername().equals(session.username())) {
            throw new SecurityException("You can only stop your own auto-bid sessions");
        }

        if (!"ACTIVE".equals(abSession.getStatus())) {
            throw new IllegalArgumentException("Session is already " + abSession.getStatus());
        }

        abSession.setStatus("STOPPED");
        abSession.addAction("STOPPED", abSession.getCurrentBid(), "Auto-bidder stopped by user.");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("status", "STOPPED");
        result.put("bidsPlaced", abSession.getBidsPlaced());
        result.put("finalBid", abSession.getCurrentBid());
        result.put("message", abSession.getCurrentBid() > 0
                ? "Auto-bidder stopped. Your last bid was $" + abSession.getCurrentBid() + "."
                : "Auto-bidder stopped. No bids were placed.");
        result.put("_links", HateoasHelper.links()
                .add("self", "/auto-bid/stop/" + sessionId)
                .add("item", "/catalogue/items/" + abSession.getItemId())
                .build());

        return result;
    }

    /**
     * POST /auto-bid/chat — Conversational Gen AI assistant.
     * Body: { itemId, message, budget? }
     * The AI can answer questions about the item, recommend strategies, and trigger auto-bid actions.
     */
    @PostMapping("/auto-bid/chat")
    @SuppressWarnings("unchecked")
    public Map<String, Object> chatWithAi(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        long itemId = requireLong(body, "itemId");
        String userMessage = requireString(body, "message");
        int budget = body.containsKey("budget") ? requireInt(body, "budget") : 0;

        // Build auction context
        Map<String, Object> auctionCtx = buildAuctionContext(itemId);

        // Check for existing auto-bid session
        var existingSession = autoBidStore.findActiveByUserAndItem(session.username(), itemId);
        auctionCtx.put("hasActiveAutoBid", existingSession.isPresent());
        if (existingSession.isPresent()) {
            auctionCtx.put("autoBidSessionId", existingSession.get().getSessionId());
            auctionCtx.put("autoBidStrategy", existingSession.get().getStrategy());
            auctionCtx.put("autoBidMaxBid", existingSession.get().getMaxBid());
            auctionCtx.put("autoBidBidsPlaced", existingSession.get().getBidsPlaced());
        }

        Map<String, Object> aiResponse = genAiAdvisor.chat(userMessage, auctionCtx, budget, session.username());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reply", aiResponse.get("reply"));
        result.put("action", aiResponse.getOrDefault("action", null));
        result.put("source", aiResponse.getOrDefault("source", "GEMINI_AI"));

        // If the AI suggests an action and user context allows it, we include action details
        // The frontend will prompt the user to confirm before executing
        if (aiResponse.containsKey("suggestedStrategy")) {
            result.put("suggestedStrategy", aiResponse.get("suggestedStrategy"));
        }
        if (aiResponse.containsKey("suggestedMaxBid")) {
            result.put("suggestedMaxBid", aiResponse.get("suggestedMaxBid"));
        }

        result.put("_links", HateoasHelper.links()
                .add("self", "/auto-bid/chat")
                .add("startAutoBid", "/auto-bid/start")
                .add("item", "/catalogue/items/" + itemId)
                .build());

        return result;
    }

    /**
     * POST /auto-bid/ai-recommend — One-shot AI recommendation.
     * Body: { itemId, budget }
     */
    @PostMapping("/auto-bid/ai-recommend")
    @SuppressWarnings("unchecked")
    public Map<String, Object> aiRecommend(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        AuthProxyController.extractToken(authHeader);

        long itemId = requireLong(body, "itemId");
        int budget = requireInt(body, "budget");

        Map<String, Object> auctionCtx = buildAuctionContext(itemId);
        Map<String, Object> recommendation = genAiAdvisor.recommend(auctionCtx, budget);

        recommendation.put("_links", HateoasHelper.links()
                .add("self", "/auto-bid/ai-recommend")
                .add("startAutoBid", "/auto-bid/start")
                .add("item", "/catalogue/items/" + itemId)
                .build());

        return recommendation;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildAuctionContext(long itemId) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        try {
            Map<String, Object> auction = restTemplate.getForObject(
                    auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);
            if (auction != null) {
                ctx.put("currentHighestBid", auction.get("currentHighestBid"));
                ctx.put("startingPrice", auction.get("startingPrice"));
                ctx.put("highestBidderUsername", auction.get("highestBidderUsername"));
                ctx.put("sellerUsername", auction.get("sellerUsername"));
                ctx.put("remainingSeconds", auction.get("remainingSeconds"));
                ctx.put("status", auction.get("status"));
                long auctionId = ((Number) auction.get("auctionId")).longValue();
                ctx.put("auctionId", auctionId);

                // Fetch bid history
                try {
                    List<Map<String, Object>> bids = restTemplate.exchange(
                            auctionBaseUrl + "/auctions/" + auctionId + "/bids",
                            HttpMethod.GET, null,
                            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                    ).getBody();
                    ctx.put("bidHistory", bids != null ? bids : List.of());
                    ctx.put("totalBids", bids != null ? bids.size() : 0);
                } catch (Exception e) {
                    ctx.put("bidHistory", List.of());
                    ctx.put("totalBids", 0);
                }
            }
        } catch (Exception e) {
            ctx.put("error", "Could not fetch auction state: " + e.getMessage());
        }
        return ctx;
    }

    // --- Helpers ---

    private Map<String, Object> buildResponse(AutoBidSession s) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", s.getSessionId());
        result.put("itemId", s.getItemId());
        result.put("maxBid", s.getMaxBid());
        result.put("strategy", s.getStrategy());
        result.put("status", s.getStatus());
        result.put("bidsPlaced", s.getBidsPlaced());
        result.put("currentBid", s.getCurrentBid());
        result.put("actionLog", s.getActionLog());
        result.put("_links", HateoasHelper.links()
                .add("self", "/auto-bid/status/" + s.getSessionId())
                .add("stop", "/auto-bid/stop/" + s.getSessionId())
                .add("item", "/catalogue/items/" + s.getItemId())
                .build());
        return result;
    }

    private long requireLong(Map<String, Object> body, String field) {
        Object raw = body.get(field);
        if (raw == null) throw new IllegalArgumentException(field + " is required");
        if (!(raw instanceof Number n)) throw new IllegalArgumentException(field + " must be a number");
        return n.longValue();
    }

    private int requireInt(Map<String, Object> body, String field) {
        Object raw = body.get(field);
        if (raw == null) throw new IllegalArgumentException(field + " is required");
        if (!(raw instanceof Number n)) throw new IllegalArgumentException(field + " must be a number");
        return n.intValue();
    }

    private String requireString(Map<String, Object> body, String field) {
        Object raw = body.get(field);
        if (raw == null || !(raw instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return s.toUpperCase();
    }
}
