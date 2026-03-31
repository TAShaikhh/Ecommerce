package com.forwardauction.payment.controller;

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
class PaymentControllerTest {

    @Autowired
    private MockMvc mvc;

    private String validPaymentJson(long auctionId, long itemId, boolean expedited) {
        double expCost = expedited ? 5.0 : 0.0;
        double total = 100.0 + 10.0 + (expedited ? 5.0 : 0.0);
        return String.format("""
                {
                  "auctionId": %d, "itemId": %d, "winnerUsername": "buyer1",
                  "itemTitle": "Test Item", "itemPrice": 100.0,
                  "shippingCost": 10.0, "expeditedShippingCost": %.1f,
                  "totalPaid": %.1f, "cardName": "John Doe",
                  "cardNumber": "1234567890123456", "expiryDate": "12/27",
                  "securityCode": "123", "shippingDays": 7, "expedited": %s
                }
                """, auctionId, itemId, expCost, total, expedited);
    }

    @Test
    void createPayment_success_returns201() throws Exception {
        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPaymentJson(101, 101, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").isNumber())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalPaid").value(110.0));
    }

    @Test
    void createPayment_withExpedited_addsToTotal() throws Exception {
        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPaymentJson(102, 102, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPaid").value(115.0))
                .andExpect(jsonPath("$.expedited").value(true));
    }

    @Test
    void createPayment_duplicateAuction_returns409() throws Exception {
        String payload = """
                {
                  "auctionId": 9901, "itemId": 9901, "winnerUsername": "buyerDup",
                  "itemTitle": "Duplicate Check", "itemPrice": 70.0,
                  "shippingCost": 10.0, "expeditedShippingCost": 0.0,
                  "totalPaid": 80.0, "cardName": "Jane",
                  "cardNumber": "1234567890123456", "expiryDate": "12/27",
                  "securityCode": "123", "shippingDays": 7, "expedited": false
                }
                """;

        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void createPayment_invalidCardNumber_returns400() throws Exception {
        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "auctionId": 2, "itemId": 2, "winnerUsername": "buyer2",
                                  "itemTitle": "Item2", "itemPrice": 50.0,
                                  "shippingCost": 10.0, "expeditedShippingCost": 0.0,
                                  "totalPaid": 60.0, "cardName": "Jane",
                                  "cardNumber": "123", "expiryDate": "12/27",
                                  "securityCode": "123", "shippingDays": 7, "expedited": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_missingSecurityCode_returns400() throws Exception {
        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "auctionId": 3, "itemId": 3, "winnerUsername": "buyer3",
                                  "itemTitle": "Item3", "itemPrice": 50.0,
                                  "shippingCost": 10.0, "expeditedShippingCost": 0.0,
                                  "totalPaid": 60.0, "cardName": "Jane",
                                  "cardNumber": "1234567890123456", "expiryDate": "12/27",
                                  "securityCode": "", "shippingDays": 7, "expedited": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPayment_notFound_returns404() throws Exception {
        mvc.perform(get("/payments/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayment_invalidExpiryFormat_returns400() throws Exception {
        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "auctionId": 4, "itemId": 4, "winnerUsername": "buyer4",
                                  "itemTitle": "Item4", "itemPrice": 50.0,
                                  "shippingCost": 10.0, "expeditedShippingCost": 0.0,
                                  "totalPaid": 60.0, "cardName": "Jane",
                                  "cardNumber": "1234567890123456", "expiryDate": "2027-12",
                                  "securityCode": "123", "shippingDays": 7, "expedited": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_negativeAuctionId_returns400() throws Exception {
        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "auctionId": -4, "itemId": 4, "winnerUsername": "buyer4",
                                  "itemTitle": "Item4", "itemPrice": 50.0,
                                  "shippingCost": 10.0, "expeditedShippingCost": 0.0,
                                  "totalPaid": 60.0, "cardName": "Jane",
                                  "cardNumber": "1234567890123456", "expiryDate": "12/27",
                                  "securityCode": "123", "shippingDays": 7, "expedited": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_negativeShippingDays_returns400() throws Exception {
        mvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "auctionId": 5, "itemId": 5, "winnerUsername": "buyer5",
                                  "itemTitle": "Item5", "itemPrice": 50.0,
                                  "shippingCost": 10.0, "expeditedShippingCost": 0.0,
                                  "totalPaid": 60.0, "cardName": "Jane",
                                  "cardNumber": "1234567890123456", "expiryDate": "12/27",
                                  "securityCode": "123", "shippingDays": -1, "expedited": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
