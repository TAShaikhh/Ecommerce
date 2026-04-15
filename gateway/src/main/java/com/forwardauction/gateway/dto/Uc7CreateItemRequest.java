package com.forwardauction.gateway.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record Uc7CreateItemRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String condition,
        @NotBlank String keywords,
        @Min(0) double shippingCost,
        @Min(0) double expeditedShippingCost,
        @Min(1) int shippingDays,
        String imageUrl,
        @Positive double startingPrice,
        @Min(10) long auctionDurationSeconds
) {}
