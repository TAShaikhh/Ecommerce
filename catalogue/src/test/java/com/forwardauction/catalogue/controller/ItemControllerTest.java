package com.forwardauction.catalogue.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void createItem_success_returns201() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerUsername":"seller1","title":"Laptop","description":"Gaming laptop","condition":"NEW","keywords":"gaming laptop","shippingCost":15.0,"expeditedShippingCost":5.0,"shippingDays":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").isNumber())
                .andExpect(jsonPath("$.title").value("Laptop"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getItemById_returnsAllFields() throws Exception {
        MvcResult createResult = mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerUsername":"seller2","title":"Phone","description":"Smartphone","condition":"USED","keywords":"phone mobile","shippingCost":10.0,"expeditedShippingCost":3.0,"shippingDays":7}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String body = createResult.getResponse().getContentAsString();
        String idStr = body.replaceAll(".*\"itemId\"\\s*:\\s*(\\d+).*", "$1");
        long itemId = Long.parseLong(idStr);

        mvc.perform(get("/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Phone"))
                .andExpect(jsonPath("$.condition").value("USED"))
                .andExpect(jsonPath("$.shippingCost").value(10.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getItemById_notFound_returns404() throws Exception {
        mvc.perform(get("/items/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByKeyword_returnsMatchingItems() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerUsername":"seller3","title":"Vintage Guitar","description":"Acoustic","condition":"USED","keywords":"guitar music vintage","shippingCost":20.0,"expeditedShippingCost":10.0,"shippingDays":10}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/items?keyword=guitar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Vintage Guitar"));
    }

    @Test
    void searchByKeyword_noResults_returnsEmptyList() throws Exception {
        mvc.perform(get("/items?keyword=xyznonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listActiveItems_returnsAll() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
