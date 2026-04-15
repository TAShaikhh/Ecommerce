package com.forwardauction.auction.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.forwardauction.auction.repo.AuctionRepository.AuctionRecord;

@Component
@Order(1)
public class IntegerBidValidator implements BidValidationStrategy {

    @Override
    public void validate(int bidAmount, AuctionRecord auction) {
        if (bidAmount <= 0) {
            throw new IllegalArgumentException("Bid amount must be a positive integer");
        }
    }
}
