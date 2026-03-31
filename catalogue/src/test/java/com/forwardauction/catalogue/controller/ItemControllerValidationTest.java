package com.forwardauction.catalogue.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerValidationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void create_missingTitle_returns400() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerUsername":"nathan","description":"d","condition":"USED","shippingCost":10.0,"expeditedShippingCost":5.0,"shippingDays":7}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingKeywords_returns400() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerUsername":"nathan","title":"Camera","description":"d","condition":"USED","shippingCost":10.0,"expeditedShippingCost":5.0,"shippingDays":7}
                                """))
                .andExpect(status().isBadRequest());
    }
}
