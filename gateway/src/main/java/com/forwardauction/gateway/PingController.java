package com.forwardauction.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class PingController {

    private final RestTemplate restTemplate;

    @Value("${services.iamBaseUrl}")
    private String iamBaseUrl;

    @Value("${services.catalogueBaseUrl}")
    private String catalogueBaseUrl;

    @Value("${services.auctionBaseUrl}")
    private String auctionBaseUrl;

    @Value("${services.paymentBaseUrl}")
    private String paymentBaseUrl;

    public PingController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/ping/iam")
    public Object pingIam() {
        return restTemplate.getForObject(iamBaseUrl + "/health", Object.class);
    }

    @GetMapping("/ping/catalogue")
    public Object pingCatalogue() {
        return restTemplate.getForObject(catalogueBaseUrl + "/health", Object.class);
    }

    @GetMapping("/ping/auction")
    public Object pingAuction() {
        return restTemplate.getForObject(auctionBaseUrl + "/health", Object.class);
    }

    @GetMapping("/ping/payment")
    public Object pingPayment() {
        return restTemplate.getForObject(paymentBaseUrl + "/health", Object.class);
    }
}