package com.forwardauction.gateway.autobid;

/**
 * Sniper strategy: stays silent until the last 10 seconds, then bids maximum.
 * Designed to win without triggering a bidding war.
 */
public class SniperBidStrategy implements BidStrategy {

    private static final long SNIPE_WINDOW_SECONDS = 10;

    @Override
    public int evaluate(AutoBidSession session, AuctionState state) {
        // Don't bid if we're already the highest bidder
        if (session.getUsername().equals(state.highestBidderUsername())) {
            return 0;
        }

        // Wait silently until the snipe window
        if (state.remainingSeconds() > SNIPE_WINDOW_SECONDS) {
            return 0;
        }

        // In the snipe window — bid as high as needed (up to max budget)
        int currentHighest = (int) state.currentHighestBid();
        int bidAmount = Math.min(session.getMaxBid(), currentHighest + 1);

        if (bidAmount <= currentHighest) {
            return bidAmount; // budget can't beat current price
        }

        return bidAmount;
    }
}
