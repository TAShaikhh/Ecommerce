package com.forwardauction.gateway;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.session.SessionStore;
import com.forwardauction.gateway.util.HateoasHelper;

@RestController
public class PaymentOrchestratorController {

    private final RestTemplate restTemplate;
    private final SessionStore sessionStore;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    @Value("${services.catalogueBaseUrl}")
    private String catalogueBaseUrl;

    @Value("${services.iamBaseUrl}")
    private String iamBaseUrl;

    @Value("${services.paymentBaseUrl}")
    private String paymentBaseUrl;

    public PaymentOrchestratorController(RestTemplate restTemplate, SessionStore sessionStore) {
        this.restTemplate = restTemplate;
        this.sessionStore = sessionStore;
    }

    @GetMapping("/payment-page/{itemId}")
    @SuppressWarnings("unchecked")
    public Map<String, Object> paymentPage(
            @PathVariable long itemId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        Map<String, Object> auction = restTemplate.getForObject(
                auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);

        String status = (String) auction.get("status");
        String result = (String) auction.get("result");
        String winner = (String) auction.get("highestBidderUsername");

        if (!"ENDED".equals(status) || !"SOLD".equals(result)) {
            throw new IllegalStateException("Auction is not ended or was not sold");
        }
        if (!session.username().equals(winner)) {
            throw new SecurityException("Only the auction winner can access the payment page");
        }

        Map<String, Object> item = restTemplate.getForObject(
                catalogueBaseUrl + "/items/" + itemId, Map.class);

        Map<String, Object> userProfile = restTemplate.getForObject(
                iamBaseUrl + "/users/by-username/" + session.username(), Map.class);

        double winningBid = ((Number) auction.get("currentHighestBid")).doubleValue();
        double shippingCost = ((Number) item.get("shippingCost")).doubleValue();
        double expeditedShippingCost = ((Number) item.get("expeditedShippingCost")).doubleValue();
        int shippingDays = ((Number) item.get("shippingDays")).intValue();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("itemId", itemId);
        response.put("auctionId", ((Number) auction.get("auctionId")).longValue());
        response.put("itemTitle", item.get("title"));
        response.put("winningBid", winningBid);
        response.put("shippingCost", shippingCost);
        response.put("expeditedShippingCost", expeditedShippingCost);
        response.put("shippingDays", shippingDays);
        response.put("totalWithStandardShipping", winningBid + shippingCost);
        response.put("totalWithExpeditedShipping", winningBid + shippingCost + expeditedShippingCost);
        response.put("shippingAddress", userProfile.get("address"));
        response.put("_links", HateoasHelper.links()
                .add("self", "/payment-page/" + itemId)
                .add("pay", "/pay")
                .add("catalogue", "/catalogue")
                .build());

        return response;
    }

    @PostMapping("/pay")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> pay(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        long itemId = requireLong(body, "itemId");
        boolean expedited = optionalBoolean(body, "expedited", false);

        Map<String, Object> auction = restTemplate.getForObject(
                auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);

        String status = (String) auction.get("status");
        String result = (String) auction.get("result");
        String winner = (String) auction.get("highestBidderUsername");
        long auctionId = ((Number) auction.get("auctionId")).longValue();

        if (!"ENDED".equals(status) || !"SOLD".equals(result)) {
            throw new IllegalStateException("Auction is not ended or was not sold");
        }
        if (!session.username().equals(winner)) {
            throw new SecurityException("Only the auction winner can make payment");
        }
        ensureNoExistingPayment(auctionId);

        Map<String, Object> item = restTemplate.getForObject(
                catalogueBaseUrl + "/items/" + itemId, Map.class);

        double winningBid = ((Number) auction.get("currentHighestBid")).doubleValue();
        double shippingCost = ((Number) item.get("shippingCost")).doubleValue();
        double expeditedShippingCost = ((Number) item.get("expeditedShippingCost")).doubleValue();
        int shippingDays = ((Number) item.get("shippingDays")).intValue();

        double total = winningBid + shippingCost;
        if (expedited) {
            total += expeditedShippingCost;
        }

        Map<String, Object> paymentBody = new LinkedHashMap<>();
        paymentBody.put("auctionId", auctionId);
        paymentBody.put("itemId", itemId);
        paymentBody.put("winnerUsername", session.username());
        paymentBody.put("itemTitle", item.get("title"));
        paymentBody.put("itemPrice", winningBid);
        paymentBody.put("shippingCost", shippingCost);
        paymentBody.put("expeditedShippingCost", expeditedShippingCost);
        paymentBody.put("totalPaid", total);
        paymentBody.put("cardName", body.get("cardName"));
        paymentBody.put("cardNumber", body.get("cardNumber"));
        paymentBody.put("expiryDate", body.get("expiryDate"));
        paymentBody.put("securityCode", body.get("securityCode"));
        paymentBody.put("shippingDays", shippingDays);
        paymentBody.put("expedited", expedited);

        Map<String, Object> paymentRes = restTemplate.postForObject(
                paymentBaseUrl + "/payments", paymentBody, Map.class);

        long paymentId = ((Number) paymentRes.get("paymentId")).longValue();

        Map<String, Object> response = new LinkedHashMap<>(paymentRes);
        response.put("_links", HateoasHelper.links()
                .add("self", "/pay")
                .add("receipt", "/receipt/" + paymentId)
                .add("catalogue", "/catalogue")
                .build());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/receipt/{paymentId}")
    @SuppressWarnings("unchecked")
    public Map<String, Object> receipt(
            @PathVariable long paymentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = AuthProxyController.extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        Map<String, Object> payment = restTemplate.getForObject(
                paymentBaseUrl + "/payments/" + paymentId, Map.class);

        String winnerUsername = String.valueOf(payment.get("winnerUsername"));
        if (!session.username().equals(winnerUsername)) {
            throw new SecurityException("Only the payment owner can view this receipt");
        }

        long itemId = ((Number) payment.get("itemId")).longValue();
        Map<String, Object> item = restTemplate.getForObject(
                catalogueBaseUrl + "/items/" + itemId, Map.class);

        int shippingDays = ((Number) payment.get("shippingDays")).intValue();
        boolean expedited = payment.get("expedited") != null && (Boolean) payment.get("expedited");

        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("transactionId", paymentId);
        receipt.put("itemTitle", payment.get("itemTitle"));
        receipt.put("winningPrice", payment.get("itemPrice"));
        receipt.put("shippingCost", payment.get("shippingCost"));
        receipt.put("expeditedShippingCost", payment.get("expeditedShippingCost"));
        receipt.put("totalPaid", payment.get("totalPaid"));
        receipt.put("cardName", payment.get("cardName"));
        receipt.put("cardLastFour", payment.get("cardLastFour"));
        receipt.put("expedited", expedited);
        receipt.put("shippingDays", shippingDays);
        receipt.put("shippingMessage", "Item will be shipped in " + shippingDays + " days");
        receipt.put("status", payment.get("status"));
        receipt.put("_links", HateoasHelper.links()
                .add("self", "/receipt/" + paymentId)
                .add("item", "/catalogue/items/" + itemId)
                .add("catalogue", "/catalogue")
                .add("home", "/health")
                .build());

        return receipt;
    }

    private void ensureNoExistingPayment(long auctionId) {
        try {
            Map<String, Object> existing = restTemplate.getForObject(
                    paymentBaseUrl + "/payments/by-auction/" + auctionId, Map.class);
            if (existing != null) {
                throw new IllegalStateException("Payment already exists for this auction");
            }
        } catch (HttpClientErrorException.NotFound ignored) {
            // No payment yet, safe to continue.
        }
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

    private boolean optionalBoolean(Map<String, Object> body, String field, boolean defaultValue) {
        Object raw = body.get(field);
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Boolean bool) {
            return bool;
        }
        throw new IllegalArgumentException(field + " must be true or false");
    }
}
