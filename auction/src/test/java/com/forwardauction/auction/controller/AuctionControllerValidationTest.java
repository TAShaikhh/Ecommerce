package com.forwardauction.auction.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuctionControllerValidationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void create_durationTooSmall_returns400() throws Exception {
        mvc.perform(post("/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itemId":1,"sellerUsername":"nathan","startingPrice":10,"durationSeconds":5}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_startingPriceZero_returns400() throws Exception {
        mvc.perform(post("/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itemId":1,"sellerUsername":"nathan","startingPrice":0,"durationSeconds":60}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_negativeItemId_returns400() throws Exception {
        mvc.perform(post("/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itemId":-1,"sellerUsername":"nathan","startingPrice":10,"durationSeconds":60}
                                """))
                .andExpect(status().isBadRequest());
    }
}
