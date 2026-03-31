package com.forwardauction.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

class BidControllerTest {

    private RestTemplate restTemplate;
    private SessionStore sessionStore;
    private BidController controller;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        sessionStore = mock(SessionStore.class);
        controller = new BidController(restTemplate, sessionStore);
        ReflectionTestUtils.setField(controller, "auctionBaseUrl", "http://auction");

        when(sessionStore.getSession("token"))
                .thenReturn(Optional.of(new SessionStore.SessionData(1L, "buyer1", 10L, 0L)));
    }

    @Test
    void placeBid_nonIntegerAmount_rejected() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.placeBid(Map.of("itemId", 10, "amount", 25.5), "Bearer token")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("integer"));
        verifyNoInteractions(restTemplate);
    }

    @SuppressWarnings("unchecked")
    @Test
    void placeBid_integerAmount_callsDownstreamAndReturns201() {
        when(restTemplate.getForObject(eq("http://auction/auctions/by-item/10"), eq(Map.class)))
                .thenReturn(Map.of("auctionId", 7L));
        when(restTemplate.postForObject(eq("http://auction/auctions/7/bids"), any(), eq(Map.class)))
                .thenReturn(Map.of("amount", 30, "currentHighestBid", 30.0));

        var response = controller.placeBid(Map.of("itemId", 10, "amount", 30), "Bearer token");

        assertEquals(201, response.getStatusCode().value());
        assertEquals(30, response.getBody().get("amount"));
    }
}
