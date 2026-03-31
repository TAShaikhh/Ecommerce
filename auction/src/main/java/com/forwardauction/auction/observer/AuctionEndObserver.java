package com.forwardauction.auction.observer;

public interface AuctionEndObserver {
    void onAuctionEnded(long auctionId, long itemId, String result);
}
