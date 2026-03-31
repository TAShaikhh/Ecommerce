package com.forwardauction.auction.observer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogueNotifier implements AuctionEndObserver {

    private static final Logger log = LoggerFactory.getLogger(CatalogueNotifier.class);

    private final RestTemplate restTemplate;

    @Value("${services.catalogueBaseUrl}")
    private String catalogueBaseUrl;

    public CatalogueNotifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void onAuctionEnded(long auctionId, long itemId, String result) {
        try {
            String status = "SOLD".equals(result) ? "SOLD" : "UNSOLD";
            restTemplate.put(
                    catalogueBaseUrl + "/items/" + itemId + "/status",
                    Map.of("status", status)
            );
            log.info("Notified catalogue: item {} -> {}", itemId, status);
        } catch (Exception e) {
            log.error("Failed to notify catalogue for item {}: {}", itemId, e.getMessage());
        }
    }
}
