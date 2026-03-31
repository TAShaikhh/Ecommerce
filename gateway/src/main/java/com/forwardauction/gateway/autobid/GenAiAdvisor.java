package com.forwardauction.gateway.autobid;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gen AI Bid Advisor — calls Google Gemini to analyze auction context
 * and produce an intelligent bidding recommendation with visible reasoning.
 *
 * Environment variable: GEMINI_API_KEY
 * Falls back to a heuristic-based recommendation if no key is configured.
 */
@Service
public class GenAiAdvisor {

    private static final Logger log = LoggerFactory.getLogger(GenAiAdvisor.class);
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api-key:#{null}}")
    private String apiKey;

    public GenAiAdvisor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Ask the Gen AI to analyze an auction and recommend a bidding strategy.
     *
     * @param auctionContext  map with keys: itemTitle, currentHighestBid, startingPrice,
     *                        remainingSeconds, totalBids, bidHistory (list), sellerUsername
     * @param userBudget      the user's stated max budget
     * @return a recommendation map with: strategy, suggestedMaxBid, reasoning, confidence, riskLevel
     */
    public Map<String, Object> recommend(Map<String, Object> auctionContext, int userBudget) {
        if (!isAvailable()) {
            return heuristicFallback(auctionContext, userBudget);
        }

        try {
            String prompt = buildPrompt(auctionContext, userBudget);
            String aiResponse = callGemini(prompt);
            return parseAiResponse(aiResponse, auctionContext, userBudget);
        } catch (Exception e) {
            log.warn("Gen AI call failed, falling back to heuristic: {}", e.getMessage());
            Map<String, Object> fallback = heuristicFallback(auctionContext, userBudget);
            fallback.put("aiError", e.getMessage());
            return fallback;
        }
    }

    /**
     * Conversational chat: send a user message with full auction context to Gemini.
     * Returns a map with 'reply' (text), and optionally 'action', 'suggestedStrategy', 'suggestedMaxBid'.
     */
    public Map<String, Object> chat(String userMessage, Map<String, Object> auctionContext,
                                     int budget, String username) {
        if (!isAvailable()) {
            return chatFallback(userMessage, auctionContext, budget);
        }

        try {
            String prompt = buildChatPrompt(userMessage, auctionContext, budget, username);
            String aiResponse = callGemini(prompt);
            return parseChatResponse(aiResponse);
        } catch (Exception e) {
            log.warn("Gen AI chat failed: {}", e.getMessage());
            Map<String, Object> fallback = chatFallback(userMessage, auctionContext, budget);
            fallback.put("aiError", e.getMessage());
            return fallback;
        }
    }

    private String buildChatPrompt(String userMessage, Map<String, Object> ctx,
                                    int budget, String username) {
        List<?> bidHistory = (List<?>) ctx.getOrDefault("bidHistory", List.of());
        StringBuilder bids = new StringBuilder();
        int count = 0;
        for (Object bid : bidHistory) {
            if (count >= 10) break;
            if (bid instanceof Map<?,?> b) {
                bids.append("  - ").append(b.get("bidderUsername")).append(": $").append(b.get("amount")).append("\n");
            }
            count++;
        }

        boolean hasAutoBid = Boolean.TRUE.equals(ctx.get("hasActiveAutoBid"));

        return """
            You are PrimeBid AI, an expert auction bidding assistant. You help users win auctions.
            You are friendly, concise, and data-driven. Keep responses under 3 sentences unless the user asks for details.
            
            CURRENT AUCTION STATE:
            - Current Highest Bid: $%s
            - Starting Price: $%s
            - Highest Bidder: %s
            - Time Remaining: %s seconds
            - Total Bids: %s
            - Auction Status: %s
            - User's Budget: %s
            - User's Username: %s
            - Auto-Bidder Active: %s
            
            RECENT BID HISTORY:
            %s
            
            AVAILABLE AUTO-BID STRATEGIES (user can activate):
            1. CONSERVATIVE — Bids +$1 when outbid. Cheapest path to winning.
            2. AGGRESSIVE — Bids 10%% above current when outbid. Intimidates competition.
            3. SNIPER — Waits until last 10 seconds, then bids. Avoids bidding wars.
            
            RULES:
            - If the user asks you to bid or start auto-bidding, include an "action" field in your response.
            - If recommending a strategy, include "suggestedStrategy" and "suggestedMaxBid".
            - Always be helpful about the item, pricing, and auction dynamics.
            - If the auction has ended, let the user know.
            
            USER MESSAGE: "%s"
            
            RESPOND IN THIS EXACT JSON FORMAT (no markdown, no code fences, pure JSON):
            {
              "reply": "<your conversational response to the user>",
              "action": "START_AUTOBID" or "STOP_AUTOBID" or null,
              "suggestedStrategy": "CONSERVATIVE" or "AGGRESSIVE" or "SNIPER" or null,
              "suggestedMaxBid": <number or null>
            }
            """.formatted(
                ctx.getOrDefault("currentHighestBid", "0"),
                ctx.getOrDefault("startingPrice", "0"),
                ctx.getOrDefault("highestBidderUsername", "none"),
                ctx.getOrDefault("remainingSeconds", "0"),
                ctx.getOrDefault("totalBids", "0"),
                ctx.getOrDefault("status", "UNKNOWN"),
                budget > 0 ? "$" + budget : "not set",
                username,
                hasAutoBid ? "Yes (" + ctx.get("autoBidStrategy") + ")" : "No",
                bids.length() > 0 ? bids.toString() : "  No bids yet.\n",
                userMessage
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseChatResponse(String rawResponse) {
        try {
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
            }
            Map<String, Object> parsed = mapper.readValue(cleaned, Map.class);
            parsed.put("source", "GEMINI_AI");
            return parsed;
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("reply", rawResponse);
            result.put("source", "GEMINI_AI_RAW");
            return result;
        }
    }

    private Map<String, Object> chatFallback(String userMessage, Map<String, Object> ctx, int budget) {
        String lower = userMessage.toLowerCase();
        Map<String, Object> result = new LinkedHashMap<>();

        double currentBid = ctx.get("currentHighestBid") != null
                ? ((Number) ctx.get("currentHighestBid")).doubleValue() : 0;
        long remaining = ctx.get("remainingSeconds") != null
                ? ((Number) ctx.get("remainingSeconds")).longValue() : 0;

        if (lower.contains("bid for me") || lower.contains("start bidding") || lower.contains("auto bid") || lower.contains("autobid")) {
            String strategy = remaining <= 15 ? "SNIPER" : (currentBid > 0 ? "CONSERVATIVE" : "AGGRESSIVE");
            int suggestedMax = budget > 0 ? budget : (int)(Math.max(currentBid, 1) * 1.5);
            result.put("reply", "I'll set up an auto-bidder for you with a " + strategy + " strategy and a max budget of $" + suggestedMax + ". You can confirm to activate it.");
            result.put("action", "START_AUTOBID");
            result.put("suggestedStrategy", strategy);
            result.put("suggestedMaxBid", suggestedMax);
        } else if (lower.contains("stop") || lower.contains("cancel")) {
            result.put("reply", "I'll stop the auto-bidder for you.");
            result.put("action", "STOP_AUTOBID");
        } else if (lower.contains("how much") || lower.contains("should i bid") || lower.contains("recommend")) {
            int suggested = (int)(Math.max(currentBid, 1) * 1.1) + 1;
            result.put("reply", "Based on the current highest bid of $" + (int)currentBid + " with " + remaining + "s remaining, I'd suggest bidding around $" + suggested + ". " +
                    (remaining <= 30 ? "Time is running short — consider a sniper approach." : "There's still time, so a conservative strategy could save you money."));
            result.put("suggestedMaxBid", suggested);
        } else {
            result.put("reply", "The current highest bid is $" + (int)currentBid + " with " + remaining + " seconds remaining. " +
                    "I can help you analyze the auction, recommend a bidding strategy, or start auto-bidding for you. Just ask!");
        }

        result.put("source", "HEURISTIC_FALLBACK");
        return result;
    }

    private String buildPrompt(Map<String, Object> ctx, int budget) {
        List<?> bidHistory = (List<?>) ctx.getOrDefault("bidHistory", List.of());
        StringBuilder bids = new StringBuilder();
        int count = 0;
        for (Object bid : bidHistory) {
            if (count >= 10) break; // limit context
            if (bid instanceof Map<?,?> b) {
                bids.append("  - ").append(b.get("bidderUsername")).append(": $").append(b.get("amount")).append("\n");
            }
            count++;
        }

        return """
            You are an expert auction bidding advisor AI. Analyze the following forward auction and recommend an optimal bidding strategy.
            
            AUCTION CONTEXT:
            - Item: %s
            - Starting Price: $%s
            - Current Highest Bid: $%s
            - Time Remaining: %s seconds
            - Total Bids Placed: %s
            - My Budget: $%d
            
            RECENT BID HISTORY (newest first):
            %s
            
            AVAILABLE STRATEGIES:
            1. CONSERVATIVE — Bids minimum increment ($1 above current) only when outbid. Best for low competition.
            2. AGGRESSIVE — Bids 10%% above current highest when outbid. Best for intimidating competitors.
            3. SNIPER — Stays silent until last 10 seconds, then bids. Best for avoiding bidding wars.
            
            RESPOND IN THIS EXACT JSON FORMAT (no markdown, no code fences, pure JSON):
            {
              "strategy": "CONSERVATIVE or AGGRESSIVE or SNIPER",
              "suggestedMaxBid": <number within budget>,
              "confidence": "HIGH or MEDIUM or LOW",
              "riskLevel": "LOW or MEDIUM or HIGH",
              "reasoning": "<2-3 sentence analysis of the auction dynamics and why this strategy is optimal>",
              "marketAnalysis": "<1-2 sentence analysis of competition patterns and price trajectory>",
              "winProbability": "<percentage estimate like '75%%'>"
            }
            """.formatted(
                ctx.getOrDefault("itemTitle", "Unknown Item"),
                ctx.getOrDefault("startingPrice", "0"),
                ctx.getOrDefault("currentHighestBid", "0"),
                ctx.getOrDefault("remainingSeconds", "0"),
                ctx.getOrDefault("totalBids", "0"),
                budget,
                bids.length() > 0 ? bids.toString() : "  No bids yet.\n"
        );
    }

    private String callGemini(String prompt) throws Exception {
        String url = GEMINI_URL + "?key=" + apiKey;

        // Build the Gemini API request body
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        if (response == null) throw new RuntimeException("Empty response from Gemini API");

        // Extract the text from the response
        JsonNode root = mapper.valueToTree(response);
        JsonNode candidates = root.path("candidates");
        if (candidates.isEmpty()) throw new RuntimeException("No candidates in Gemini response");

        String text = candidates.get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        log.info("Gemini raw response: {}", text);
        return text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAiResponse(String rawResponse, Map<String, Object> ctx, int budget) {
        try {
            // Clean up response — strip markdown fences if present
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            Map<String, Object> parsed = mapper.readValue(cleaned, Map.class);
            parsed.put("source", "GEMINI_AI");
            parsed.put("model", "gemini-2.0-flash");

            // Validate and clamp suggestedMaxBid within budget
            if (parsed.containsKey("suggestedMaxBid")) {
                int suggested = ((Number) parsed.get("suggestedMaxBid")).intValue();
                parsed.put("suggestedMaxBid", Math.min(suggested, budget));
            }

            // Validate strategy is one of the known ones
            String strategy = (String) parsed.getOrDefault("strategy", "CONSERVATIVE");
            if (!strategy.equals("CONSERVATIVE") && !strategy.equals("AGGRESSIVE") && !strategy.equals("SNIPER")) {
                parsed.put("strategy", "CONSERVATIVE");
            }

            return parsed;
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON: {}", e.getMessage());
            // Return the raw text as reasoning with a fallback strategy
            Map<String, Object> fallback = heuristicFallback(ctx, budget);
            fallback.put("source", "GEMINI_AI_PARTIAL");
            fallback.put("reasoning", rawResponse);
            return fallback;
        }
    }

    /**
     * Heuristic fallback when Gemini API is unavailable.
     * Uses rule-based analysis of auction dynamics.
     */
    private Map<String, Object> heuristicFallback(Map<String, Object> ctx, int budget) {
        double currentBid = ctx.get("currentHighestBid") != null
                ? ((Number) ctx.get("currentHighestBid")).doubleValue() : 0;
        long remaining = ctx.get("remainingSeconds") != null
                ? ((Number) ctx.get("remainingSeconds")).longValue() : 60;
        int totalBids = ctx.get("totalBids") != null
                ? ((Number) ctx.get("totalBids")).intValue() : 0;

        String strategy;
        String reasoning;
        String confidence;
        String riskLevel;
        int suggestedMax;

        if (remaining <= 15) {
            // Very little time — snipe
            strategy = "SNIPER";
            suggestedMax = Math.min(budget, (int)(currentBid * 1.05) + 1);
            reasoning = "With only " + remaining + " seconds remaining, a sniper strategy is optimal to avoid triggering last-second bidding wars. The AI recommends placing a single decisive bid in the final moments.";
            confidence = "HIGH";
            riskLevel = "MEDIUM";
        } else if (totalBids >= 5 || currentBid > budget * 0.6) {
            // High competition — be conservative
            strategy = "CONSERVATIVE";
            suggestedMax = Math.min(budget, (int)(currentBid * 1.15));
            reasoning = "This auction shows high competition with " + totalBids + " bids already placed. A conservative approach minimizes cost while maintaining competitiveness. The AI recommends careful incremental bidding.";
            confidence = "MEDIUM";
            riskLevel = "LOW";
        } else {
            // Low competition — be aggressive
            strategy = "AGGRESSIVE";
            suggestedMax = Math.min(budget, (int)(currentBid * 1.30));
            reasoning = "Low competition detected with only " + totalBids + " bid(s) and " + remaining + " seconds remaining. An aggressive strategy can establish dominance early and discourage competitors.";
            confidence = "HIGH";
            riskLevel = "MEDIUM";
        }

        // Clamp suggested max to at least current+1
        suggestedMax = Math.max(suggestedMax, (int) currentBid + 1);
        suggestedMax = Math.min(suggestedMax, budget);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("strategy", strategy);
        result.put("suggestedMaxBid", suggestedMax);
        result.put("confidence", confidence);
        result.put("riskLevel", riskLevel);
        result.put("reasoning", reasoning);
        result.put("marketAnalysis", totalBids > 3
                ? "Multiple bidders are actively competing, suggesting strong demand for this item."
                : "Limited bidder activity suggests an opportunity to win at a favorable price.");
        result.put("winProbability", suggestedMax >= currentBid * 1.2 ? "78%" : "55%");
        result.put("source", "HEURISTIC_FALLBACK");
        result.put("model", "rule-based-v1");

        return result;
    }
}
