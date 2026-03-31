package com.forwardauction.auction.strategy;

import com.forwardauction.auction.repo.AuctionRepository.AuctionRecord;

public interface BidValidationStrategy {
    void validate(int bidAmount, AuctionRecord auction);
}
