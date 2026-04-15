package com.forwardauction.auction.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forwardauction.auction.repo.AuctionRepository;
import com.forwardauction.auction.repo.AuctionRepository.AuctionRecord;
import com.forwardauction.auction.repo.BidRepository;
import com.forwardauction.auction.repo.BidRepository.BidRecord;
import com.forwardauction.auction.strategy.BidValidationStrategy;

@Service
public class AuctionService {

    private final AuctionRepository auctions;
    private final BidRepository bids;
    private final List<BidValidationStrategy> validators;

    public AuctionService(AuctionRepository auctions, BidRepository bids, List<BidValidationStrategy> validators) {
        this.auctions = auctions;
        this.bids = bids;
        this.validators = validators;
    }

    public AuctionRecord getAuction(long auctionId) {
        return auctions.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("Auction not found: " + auctionId));
    }

    public AuctionRecord getAuctionByItemId(long itemId) {
        return auctions.findByItemId(itemId)
                .orElseThrow(() -> new NoSuchElementException("No auction found for item: " + itemId));
    }

    @Transactional
    public BidRecord placeBid(long auctionId, String bidderUsername, int amount) {
        AuctionRecord auction = getAuction(auctionId);

        if (!"ACTIVE".equals(auction.status())) {
            throw new IllegalStateException("Auction is no longer active");
        }
        if (auction.remainingSeconds() <= 0) {
            throw new IllegalStateException("Auction has expired");
        }
        if (auction.sellerUsername().equals(bidderUsername)) {
            throw new IllegalArgumentException("Seller cannot bid on their own auction");
        }

        for (BidValidationStrategy v : validators) {
            v.validate(amount, auction);
        }

        long now = System.currentTimeMillis() / 1000;
        boolean updated = auctions.tryUpdateBidIfHigher(auctionId, amount, bidderUsername, now);
        if (!updated) {
            AuctionRecord latest = getAuction(auctionId);
            if (!"ACTIVE".equals(latest.status()) || latest.remainingSeconds() <= 0) {
                throw new IllegalStateException("Auction is no longer active");
            }
            for (BidValidationStrategy v : validators) {
                v.validate(amount, latest);
            }
            throw new IllegalStateException("Bid could not be placed due to a concurrent update");
        }

        long bidId = bids.create(auctionId, bidderUsername, amount);
        return new BidRecord(bidId, auctionId, bidderUsername, amount, now);
    }

    public List<BidRecord> getBidHistory(long auctionId) {
        getAuction(auctionId);
        return bids.findByAuctionId(auctionId);
    }
}
