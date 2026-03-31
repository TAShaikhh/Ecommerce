package com.forwardauction.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.session.SessionStore;

class PaymentOrchestratorControllerTest {

    private RestTemplate restTemplate;
    private SessionStore sessionStore;
    private PaymentOrchestratorController controller;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        sessionStore = mock(SessionStore.class);
        controller = new PaymentOrchestratorController(restTemplate, sessionStore);
        ReflectionTestUtils.setField(controller, "auctionBaseUrl", "http://auction");
        ReflectionTestUtils.setField(controller, "paymentBaseUrl", "http://payment");
        ReflectionTestUtils.setField(controller, "catalogueBaseUrl", "http://catalogue");

        when(sessionStore.getSession("token"))
                .thenReturn(Optional.of(new SessionStore.SessionData(1L, "alice", 11L, 0L)));
    }

    @Test
    void pay_missingItemId_rejectedBeforeDownstreamCalls() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.pay(Map.of("expedited", true), "Bearer token")
        );

        assertTrue(ex.getMessage().contains("itemId is required"));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void pay_existingPayment_rejected() {
        when(restTemplate.getForObject(eq("http://auction/auctions/by-item/11"), eq(Map.class)))
                .thenReturn(Map.of(
                        "status", "ENDED",
                        "result", "SOLD",
                        "highestBidderUsername", "alice",
                        "auctionId", 77L,
                        "currentHighestBid", 120.0
                ));
        when(restTemplate.getForObject(eq("http://payment/payments/by-auction/77"), eq(Map.class)))
                .thenReturn(Map.of("paymentId", 5L));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> controller.pay(Map.of("itemId", 11L, "expedited", false), "Bearer token")
        );

        assertTrue(ex.getMessage().contains("Payment already exists"));
    }

    @Test
    void receipt_nonOwner_forbidden() {
        when(restTemplate.getForObject(eq("http://payment/payments/99"), eq(Map.class)))
                .thenReturn(Map.of("winnerUsername", "bob"));

        SecurityException ex = assertThrows(
                SecurityException.class,
                () -> controller.receipt(99L, "Bearer token")
        );

        assertTrue(ex.getMessage().contains("Only the payment owner"));
    }

    @Test
    void receipt_owner_returnsReceipt() {
        when(restTemplate.getForObject(eq("http://payment/payments/42"), eq(Map.class)))
                .thenReturn(Map.ofEntries(
                        Map.entry("winnerUsername", "alice"),
                        Map.entry("itemId", 11L),
                        Map.entry("shippingDays", 5),
                        Map.entry("expedited", true),
                        Map.entry("itemTitle", "Laptop"),
                        Map.entry("itemPrice", 100.0),
                        Map.entry("shippingCost", 10.0),
                        Map.entry("expeditedShippingCost", 5.0),
                        Map.entry("totalPaid", 115.0),
                        Map.entry("cardName", "Alice"),
                        Map.entry("cardLastFour", "1111"),
                        Map.entry("status", "COMPLETED")
                ));
        when(restTemplate.getForObject(eq("http://catalogue/items/11"), eq(Map.class)))
                .thenReturn(Map.of("id", 11L));

        Map<String, Object> receipt = controller.receipt(42L, "Bearer token");

        assertEquals(42L, receipt.get("transactionId"));
        assertEquals("Item will be shipped in 5 days", receipt.get("shippingMessage"));
    }
}
