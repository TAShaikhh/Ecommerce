package com.forwardauction.auction.dto;

public record BidResponse(
        long bidId,
        long auctionId,
        String bidderUsername,
        int amount,
        double currentHighestBid,
        String highestBidder,
        long remainingSeconds,
        int bidCount
) {}
