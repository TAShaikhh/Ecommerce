package com.forwardauction.gateway;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import com.forwardauction.gateway.session.SessionStore;
import com.forwardauction.gateway.util.HateoasHelper;

@RestController
public class BidController {

    private final RestTemplate restTemplate;
    private final SessionStore sessionStore;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    public BidController(RestTemplate restTemplate, SessionStore sessionStore) {
        this.restTemplate = restTemplate;
        this.sessionStore = sessionStore;
    }

    @PostMapping("/bid")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> placeBid(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        int amount = requireInteger(body, "amount");

        Long itemId = body.get("itemId") != null
                ? requireLong(body, "itemId")
                : session.selectedItemId();

        if (itemId == null) {
            throw new IllegalArgumentException("No item selected. Use POST /catalogue/select/{itemId} first, or provide itemId in the request body");
        }

        if (session.selectedItemId() == null || !session.selectedItemId().equals(itemId)) {
            throw new IllegalArgumentException("You must select this item first via POST /catalogue/select/" + itemId);
        }

        Map<String, Object> auction = restTemplate.getForObject(
                auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);
        long auctionId = ((Number) auction.get("auctionId")).longValue();

        Map<String, Object> bidBody = Map.of(
                "bidderUsername", session.username(),
                "amount", amount
        );

        Map<String, Object> bidRes = restTemplate.postForObject(
                auctionBaseUrl + "/auctions/" + auctionId + "/bids",
                bidBody, Map.class);

        Map<String, Object> result = new LinkedHashMap<>(bidRes);
        result.put("_links", HateoasHelper.links()
                .add("self", "/bid")
                .add("bidHistory", "/bid/history/" + itemId)
                .add("auctionState", "/catalogue/items/" + itemId)
                .add("catalogue", "/catalogue")
                .build());

        return ResponseEntity.status(201).body(result);
    }

    @GetMapping("/bid/history/{itemId}")
    @SuppressWarnings("unchecked")
    public Map<String, Object> bidHistory(@PathVariable long itemId) {
        Map<String, Object> auction = restTemplate.getForObject(
                auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);
        long auctionId = ((Number) auction.get("auctionId")).longValue();

        List<Map<String, Object>> bids = restTemplate.exchange(
                auctionBaseUrl + "/auctions/" + auctionId + "/bids",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", itemId);
        result.put("auctionId", auctionId);
        result.put("bids", bids);
        result.put("_links", HateoasHelper.links()
                .add("self", "/bid/history/" + itemId)
                .add("bid", "/bid")
                .add("item", "/catalogue/items/" + itemId)
                .add("catalogue", "/catalogue")
                .build());

        return result;
    }

    private int requireInteger(Map<String, Object> body, String field) {
        Object raw = body.get(field);
        if (raw == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (!(raw instanceof Number number)) {
            throw new IllegalArgumentException(field + " must be an integer");
        }

        BigDecimal decimal = new BigDecimal(number.toString());
        if (decimal.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(field + " must be an integer");
        }
        return decimal.intValueExact();
    }

    private long requireLong(Map<String, Object> body, String field) {
        Object raw = body.get(field);
        if (raw == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (!(raw instanceof Number number)) {
            throw new IllegalArgumentException(field + " must be an integer");
        }

        BigDecimal decimal = new BigDecimal(number.toString());
        if (decimal.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(field + " must be an integer");
        }
        return decimal.longValueExact();
    }
}
