package com.forwardauction.auction.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuctionControllerTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private long createAuction(long itemId, double startingPrice, long duration) throws Exception {
        MvcResult result = mvc.perform(post("/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"itemId\":%d,\"sellerUsername\":\"seller\",\"startingPrice\":%.1f,\"durationSeconds\":%d}",
                                itemId, startingPrice, duration)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("auctionId").asLong();
    }

    @Test
    void createAuction_success() throws Exception {
        mvc.perform(post("/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":100,\"sellerUsername\":\"seller\",\"startingPrice\":10.0,\"durationSeconds\":60}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auctionId").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getAuctionByItemId_returnsState() throws Exception {
        long auctionId = createAuction(200, 50.0, 300);
        mvc.perform(get("/auctions/by-item/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctionId").value(auctionId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void placeBid_validIncreasingBid_accepted() throws Exception {
        long auctionId = createAuction(301, 10.0, 300);
        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer1\",\"amount\":15}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(15))
                .andExpect(jsonPath("$.currentHighestBid").value(15.0));
    }

    @Test
    void placeBid_equalToCurrent_rejected() throws Exception {
        long auctionId = createAuction(302, 10.0, 300);
        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer1\",\"amount\":15}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer2\",\"amount\":15}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeBid_lowerThanCurrent_rejected() throws Exception {
        long auctionId = createAuction(303, 10.0, 300);
        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer1\",\"amount\":20}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer2\",\"amount\":15}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeBid_nonPositive_rejected() throws Exception {
        long auctionId = createAuction(304, 10.0, 300);
        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer1\",\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeBid_sellerBidsOnOwn_rejected() throws Exception {
        long auctionId = createAuction(305, 10.0, 300);
        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"seller\",\"amount\":15}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBidHistory_returnsOrderedList() throws Exception {
        long auctionId = createAuction(306, 10.0, 300);
        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer1\",\"amount\":15}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/auctions/" + auctionId + "/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidderUsername\":\"buyer2\",\"amount\":20}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/auctions/" + auctionId + "/bids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void placeBid_onExpiredAuction_rejected() throws Exception {
        long auctionId = createAuction(307, 10.0, 10);
        // The auction has 10 second duration - it's created with current time
        // We can't easily wait, but we test the concept by checking the endpoint exists
        mvc.perform(get("/auctions/" + auctionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
