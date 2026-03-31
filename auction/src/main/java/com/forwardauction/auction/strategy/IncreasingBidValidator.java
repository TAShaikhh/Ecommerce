package com.forwardauction.auction.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.forwardauction.auction.repo.AuctionRepository.AuctionRecord;

@Component
@Order(2)
public class IncreasingBidValidator implements BidValidationStrategy {

    @Override
    public void validate(int bidAmount, AuctionRecord auction) {
        double currentHighest = auction.currentHighestBid();
        double startingPrice = auction.startingPrice();
        double minimum = Math.max(currentHighest, startingPrice);

        if (bidAmount <= minimum) {
            throw new IllegalArgumentException(
                    "Bid must be strictly higher than current highest bid of " + (int) minimum);
        }
    }
}
