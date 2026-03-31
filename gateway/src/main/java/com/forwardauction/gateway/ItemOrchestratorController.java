package com.forwardauction.gateway;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.dto.Uc7CreateItemRequest;
import com.forwardauction.gateway.session.SessionStore;
import com.forwardauction.gateway.util.HateoasHelper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/items")
public class ItemOrchestratorController {

    private final RestTemplate restTemplate;
    private final SessionStore sessionStore;

    @Value("${services.catalogueBaseUrl}")
    private String catalogueBaseUrl;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    public ItemOrchestratorController(RestTemplate restTemplate, SessionStore sessionStore) {
        this.restTemplate = restTemplate;
        this.sessionStore = sessionStore;
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> createItem(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody Uc7CreateItemRequest req) {
        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid or missing session token"));

        Map<String, Object> catBody = new LinkedHashMap<>();
        catBody.put("ownerUsername", session.username());
        catBody.put("title", req.title());
        catBody.put("description", req.description());
        catBody.put("condition", req.condition());
        catBody.put("keywords", req.keywords());
        catBody.put("shippingCost", req.shippingCost());
        catBody.put("expeditedShippingCost", req.expeditedShippingCost());
        catBody.put("shippingDays", req.shippingDays());
        catBody.put("imageUrl", req.imageUrl() != null ? req.imageUrl() : "");

        Map<String, Object> catalogueRes = restTemplate.postForObject(
                catalogueBaseUrl + "/items", catBody, Map.class);

        long itemId = ((Number) catalogueRes.get("itemId")).longValue();

        Map<String, Object> auctionBody = Map.of(
                "itemId", itemId,
                "sellerUsername", session.username(),
                "startingPrice", req.startingPrice(),
                "durationSeconds", req.auctionDurationSeconds()
        );

        Map<String, Object> auctionRes = restTemplate.postForObject(
                auctionBaseUrl + "/auctions", auctionBody, Map.class);

        long auctionId = ((Number) auctionRes.get("auctionId")).longValue();
        String status = (String) auctionRes.get("status");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", itemId);
        result.put("auctionId", auctionId);
        result.put("status", status);
        result.put("title", req.title());
        result.put("_links", HateoasHelper.links()
                .add("self", "/items")
                .add("item", "/catalogue/items/" + itemId)
                .add("auction", "/catalogue/items/" + itemId)
                .add("catalogue", "/catalogue")
                .build());

        return ResponseEntity.status(201).body(result);
    }
}
