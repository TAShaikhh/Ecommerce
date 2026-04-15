package com.forwardauction.gateway.autobid;

/**
 * Conservative strategy: bids the minimum increment (+$1) only when outbid.
 * Ideal for users who want to win at the lowest possible price.
 */
public class ConservativeBidStrategy implements BidStrategy {

    @Override
    public int evaluate(AutoBidSession session, AuctionState state) {
        // Don't bid if we're already the highest bidder
        if (session.getUsername().equals(state.highestBidderUsername())) {
            return 0;
        }

        int currentHighest = (int) state.currentHighestBid();
        int bidAmount = currentHighest + 1;

        // If the minimum bid exceeds our budget, signal budget exceeded
        if (bidAmount > session.getMaxBid()) {
            return bidAmount; // caller will detect this exceeds maxBid and stop
        }

        return bidAmount;
    }
}
