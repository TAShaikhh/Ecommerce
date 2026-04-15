package com.forwardauction.auction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateAuctionRequest(
        @Positive long itemId,
        @NotBlank String sellerUsername,
        @Positive double startingPrice,
        @Min(10) long durationSeconds
) {}
