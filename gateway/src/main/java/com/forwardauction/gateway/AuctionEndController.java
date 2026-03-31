package com.forwardauction.gateway;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.session.SessionStore;
import com.forwardauction.gateway.util.HateoasHelper;

@RestController
public class AuctionEndController {

    private final RestTemplate restTemplate;
    private final SessionStore sessionStore;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    public AuctionEndController(RestTemplate restTemplate, SessionStore sessionStore) {
        this.restTemplate = restTemplate;
        this.sessionStore = sessionStore;
    }

    @GetMapping("/auction-result/{itemId}")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAuctionResult(
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
        long auctionId = ((Number) auction.get("auctionId")).longValue();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("itemId", itemId);
        response.put("auctionId", auctionId);
        response.put("status", status);
        response.put("result", result);
        response.put("winner", winner);

        boolean isWinner = session.username().equals(winner);
        response.put("isWinner", isWinner);

        if ("ENDED".equals(status) && "SOLD".equals(result) && isWinner) {
            response.put("message", "Congratulations! You won this auction.");
            response.put("_links", HateoasHelper.links()
                    .add("self", "/auction-result/" + itemId)
                    .add("payNow", "/payment-page/" + itemId)
                    .add("catalogue", "/catalogue")
                    .build());
        } else if ("ENDED".equals(status)) {
            String msg = "UNSOLD".equals(result)
                    ? "This auction ended with no bids"
                    : "You did not win this auction";
            response.put("message", msg);
            response.put("_links", HateoasHelper.links()
                    .add("self", "/auction-result/" + itemId)
                    .add("catalogue", "/catalogue")
                    .build());
        } else {
            response.put("message", "Auction is still active");
            response.put("_links", HateoasHelper.links()
                    .add("self", "/auction-result/" + itemId)
                    .add("bid", "/bid")
                    .add("catalogue", "/catalogue")
                    .build());
        }

        return response;
    }
}
