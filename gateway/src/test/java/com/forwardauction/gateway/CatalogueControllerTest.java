package com.forwardauction.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.session.SessionStore;

class CatalogueControllerTest {

    private RestTemplate restTemplate;
    private SessionStore sessionStore;
    private CatalogueController controller;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        sessionStore = mock(SessionStore.class);
        controller = new CatalogueController(restTemplate, sessionStore);

        ReflectionTestUtils.setField(controller, "catalogueBaseUrl", "http://catalogue");
        ReflectionTestUtils.setField(controller, "auctionBaseUrl", "http://auction");

        when(sessionStore.getSession("token"))
                .thenReturn(Optional.of(new SessionStore.SessionData(1L, "buyer1", null, 0L)));
    }

    @Test
    void selectItem_activeItemAndAuction_updatesSelection() {
        when(restTemplate.getForObject(eq("http://catalogue/items/10"), eq(Map.class)))
                .thenReturn(Map.of("status", "ACTIVE"));
        when(restTemplate.getForObject(eq("http://auction/auctions/by-item/10"), eq(Map.class)))
                .thenReturn(Map.of("status", "ACTIVE"));

        Map<String, Object> res = controller.selectItem(10L, "Bearer token");

        verify(sessionStore).selectItem("token", 10L);
        assertEquals(10L, res.get("selectedItemId"));
    }

    @Test
    void selectItem_inactiveItem_rejected() {
        when(restTemplate.getForObject(eq("http://catalogue/items/10"), eq(Map.class)))
                .thenReturn(Map.of("status", "SOLD"));

        assertThrows(IllegalStateException.class, () -> controller.selectItem(10L, "Bearer token"));
        verify(sessionStore).getSession("token");
        verify(sessionStore, never()).selectItem("token", 10L);
        verifyNoMoreInteractions(sessionStore);
    }

    @Test
    void selectItem_inactiveAuction_rejected() {
        when(restTemplate.getForObject(eq("http://catalogue/items/10"), eq(Map.class)))
                .thenReturn(Map.of("status", "ACTIVE"));
        when(restTemplate.getForObject(eq("http://auction/auctions/by-item/10"), eq(Map.class)))
                .thenReturn(Map.of("status", "ENDED"));

        assertThrows(IllegalStateException.class, () -> controller.selectItem(10L, "Bearer token"));
        verify(sessionStore).getSession("token");
        verify(sessionStore, never()).selectItem("token", 10L);
        verifyNoMoreInteractions(sessionStore);
    }
}
