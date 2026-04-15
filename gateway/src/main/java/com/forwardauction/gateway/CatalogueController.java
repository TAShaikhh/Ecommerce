package com.forwardauction.gateway;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.forwardauction.gateway.session.SessionStore;
import com.forwardauction.gateway.util.HateoasHelper;

@RestController
@RequestMapping("/catalogue")
public class CatalogueController {

    private final RestTemplate restTemplate;
    private final SessionStore sessionStore;

    @Value("${services.catalogueBaseUrl}")
    private String catalogueBaseUrl;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    public CatalogueController(RestTemplate restTemplate, SessionStore sessionStore) {
        this.restTemplate = restTemplate;
        this.sessionStore = sessionStore;
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public Map<String, Object> search(@RequestParam(required = false) String keyword) {
        UriComponentsBuilder requestUri = UriComponentsBuilder.fromUriString(catalogueBaseUrl + "/items");
        UriComponentsBuilder selfUri = UriComponentsBuilder.fromPath("/catalogue");
        if (keyword != null && !keyword.isBlank()) {
            requestUri.queryParam("keyword", keyword);
            selfUri.queryParam("keyword", keyword);
        }
        String url = requestUri.toUriString();

        List<Map<String, Object>> items = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        List<Map<String, Object>> enriched = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> item : items) {
                Map<String, Object> merged = new LinkedHashMap<>(item);
                long itemId = ((Number) item.get("id")).longValue();

                try {
                    Map<String, Object> auction = restTemplate.getForObject(
                            auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);
                    merged.put("auction", auction);
                } catch (Exception e) {
                    merged.put("auction", null);
                }

                merged.put("_links", HateoasHelper.links()
                        .add("self", "/catalogue/items/" + itemId)
                        .add("selectItem", "/catalogue/select/" + itemId)
                        .add("bid", "/bid")
                        .build());

                enriched.add(merged);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", enriched);
        result.put("count", enriched.size());
        result.put("_links", HateoasHelper.links()
                .add("self", selfUri.toUriString())
                .addTemplated("search", "/catalogue?keyword={keyword}")
                .add("createItem", "/items")
                .build());

        return result;
    }

    @GetMapping("/items/{itemId}")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getItemDetail(@PathVariable long itemId) {
        Map<String, Object> item = restTemplate.getForObject(
                catalogueBaseUrl + "/items/" + itemId, Map.class);

        Map<String, Object> result = new LinkedHashMap<>(item);

        try {
            Map<String, Object> auction = restTemplate.getForObject(
                    auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);
            result.put("auction", auction);
        } catch (Exception e) {
            result.put("auction", null);
        }

        result.put("_links", HateoasHelper.links()
                .add("self", "/catalogue/items/" + itemId)
                .add("selectItem", "/catalogue/select/" + itemId)
                .add("bid", "/bid")
                .add("catalogue", "/catalogue")
                .build());

        return result;
    }

    @PostMapping("/select/{itemId}")
    public Map<String, Object> selectItem(@PathVariable long itemId,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = AuthProxyController.extractToken(authHeader);
        sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid session"));

        Map<String, Object> item = restTemplate.getForObject(
                catalogueBaseUrl + "/items/" + itemId, Map.class);
        String itemStatus = String.valueOf(item.get("status"));
        if (!"ACTIVE".equals(itemStatus)) {
            throw new IllegalStateException("Only ACTIVE items can be selected");
        }

        Map<String, Object> auction = restTemplate.getForObject(
                auctionBaseUrl + "/auctions/by-item/" + itemId, Map.class);
        String auctionStatus = String.valueOf(auction.get("status"));
        if (!"ACTIVE".equals(auctionStatus)) {
            throw new IllegalStateException("Auction is not active for this item");
        }

        sessionStore.selectItem(token, itemId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("selectedItemId", itemId);
        result.put("message", "Item selected for bidding");
        result.put("_links", HateoasHelper.links()
                .add("self", "/catalogue/select/" + itemId)
                .add("item", "/catalogue/items/" + itemId)
                .add("bid", "/bid")
                .add("catalogue", "/catalogue")
                .build());

        return result;
    }
}
