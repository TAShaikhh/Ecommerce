package com.forwardauction.gateway.autobid;

/**
 * Strategy interface for AI auto-bid decision making.
 * Each implementation decides whether to bid and at what amount
 * given the current auction state and session configuration.
 */
public interface BidStrategy {

    /**
     * Evaluate the current auction state and decide a bid amount.
     *
     * @param session the auto-bid session configuration
     * @param state   current auction state from the Auction service
     * @return the amount to bid, or 0 if no bid should be placed this tick
     */
    int evaluate(AutoBidSession session, AuctionState state);

    /** Snapshot of live auction data fetched from the Auction service. */
    record AuctionState(
            long auctionId,
            long itemId,
            String status,
            String result,
            double currentHighestBid,
            String highestBidderUsername,
            long remainingSeconds
    ) {}
}
