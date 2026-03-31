package com.forwardauction.auction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BidRequest(
        @NotBlank String bidderUsername,
        @Min(1) int amount
) {}
