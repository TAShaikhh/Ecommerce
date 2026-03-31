package com.forwardauction.gateway.autobid;

/**
 * Aggressive strategy: bids 10% above current highest bid when outbid.
 * Designed to intimidate competitors and establish dominance quickly.
 */
public class AggressiveBidStrategy implements BidStrategy {

    private static final double ESCALATION_FACTOR = 1.10; // 10% above

    @Override
    public int evaluate(AutoBidSession session, AuctionState state) {
        // Don't bid if we're already the highest bidder
        if (session.getUsername().equals(state.highestBidderUsername())) {
            return 0;
        }

        int currentHighest = (int) state.currentHighestBid();
        // Bid 10% above current highest, minimum +1
        int bidAmount = Math.max(currentHighest + 1, (int) Math.ceil(currentHighest * ESCALATION_FACTOR));

        if (bidAmount > session.getMaxBid()) {
            return bidAmount; // caller detects budget exceeded
        }

        return bidAmount;
    }
}
