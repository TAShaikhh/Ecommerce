package com.forwardauction.catalogue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateItemRequest(
        @NotBlank String ownerUsername,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String condition,
        @NotBlank String keywords,
        @Min(0) double shippingCost,
        @Min(0) double expeditedShippingCost,
        @Min(1) int shippingDays,
        String imageUrl
) {}
