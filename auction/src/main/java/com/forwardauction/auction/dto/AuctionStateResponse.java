package com.forwardauction.auction.dto;

public record AuctionStateResponse(
        long auctionId,
        long itemId,
        String sellerUsername,
        double startingPrice,
        double currentHighestBid,
        String highestBidderUsername,
        String status,
        String result,
        long remainingSeconds
) {}
