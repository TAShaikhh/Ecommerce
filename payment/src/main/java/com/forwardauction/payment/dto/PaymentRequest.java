package com.forwardauction.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @Positive long auctionId,
        @Positive long itemId,
        @NotBlank String winnerUsername,
        @NotBlank String itemTitle,
        @Min(0) double itemPrice,
        @Min(0) double shippingCost,
        @Min(0) double expeditedShippingCost,
        double totalPaid,
        @NotBlank String cardName,
        @NotBlank String cardNumber,
        @NotBlank String expiryDate,
        @NotBlank String securityCode,
        @Min(1) int shippingDays,
        boolean expedited
) {}
