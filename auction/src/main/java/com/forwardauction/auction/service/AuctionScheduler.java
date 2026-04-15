package com.forwardauction.auction.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.forwardauction.auction.observer.AuctionEndObserver;
import com.forwardauction.auction.repo.AuctionRepository;
import com.forwardauction.auction.repo.AuctionRepository.AuctionRecord;

@Component
public class AuctionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuctionScheduler.class);

    private final AuctionRepository auctions;
    private final List<AuctionEndObserver> observers;

    public AuctionScheduler(AuctionRepository auctions, List<AuctionEndObserver> observers) {
        this.auctions = auctions;
        this.observers = observers;
    }

    @Scheduled(fixedRate = 5000)
    public void checkExpiredAuctions() {
        List<AuctionRecord> expired = auctions.findExpiredActive();
        for (AuctionRecord a : expired) {
            String result = (a.highestBidderUsername() != null && !a.highestBidderUsername().isBlank())
                    ? "SOLD" : "UNSOLD";
            auctions.endAuction(a.id(), result);
            log.info("Auction {} ended: {}", a.id(), result);

            for (AuctionEndObserver obs : observers) {
                obs.onAuctionEnded(a.id(), a.itemId(), result);
            }
        }
    }
}
