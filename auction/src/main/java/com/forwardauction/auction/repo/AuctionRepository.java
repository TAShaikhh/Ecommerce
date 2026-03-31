package com.forwardauction.auction.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuctionRepository {

    private final JdbcTemplate jdbc;

    public static final RowMapper<AuctionRecord> ROW_MAPPER = (rs, rowNum) -> new AuctionRecord(
            rs.getLong("id"),
            rs.getLong("item_id"),
            rs.getString("seller_username"),
            rs.getDouble("starting_price"),
            rs.getDouble("current_highest_bid"),
            rs.getString("highest_bidder_username"),
            rs.getString("status"),
            rs.getString("result"),
            rs.getLong("created_at_epoch"),
            rs.getLong("duration_seconds")
    );

    public AuctionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long create(long itemId, String sellerUsername, double startingPrice, long durationSeconds) {
        long now = System.currentTimeMillis() / 1000;
        jdbc.update(
                "INSERT INTO auctions(item_id, seller_username, starting_price, current_highest_bid, status, created_at_epoch, duration_seconds) VALUES(?,?,?,?,?,?,?)",
                itemId, sellerUsername, startingPrice, 0, "ACTIVE", now, durationSeconds
        );
        Long id = jdbc.queryForObject(
                "SELECT id FROM auctions WHERE item_id=? ORDER BY id DESC LIMIT 1",
                Long.class, itemId
        );
        return id == null ? -1 : id;
    }

    public Optional<AuctionRecord> findById(long id) {
        return jdbc.query("SELECT * FROM auctions WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public Optional<AuctionRecord> findByItemId(long itemId) {
        return jdbc.query("SELECT * FROM auctions WHERE item_id = ?", ROW_MAPPER, itemId)
                .stream().findFirst();
    }

    public List<AuctionRecord> findExpiredActive() {
        long now = System.currentTimeMillis() / 1000;
        return jdbc.query(
                "SELECT * FROM auctions WHERE status = 'ACTIVE' AND (created_at_epoch + duration_seconds) < ?",
                ROW_MAPPER, now
        );
    }

    public void updateBid(long auctionId, double amount, String bidderUsername) {
        jdbc.update(
                "UPDATE auctions SET current_highest_bid = ?, highest_bidder_username = ? WHERE id = ?",
                amount, bidderUsername, auctionId
        );
    }

    public boolean tryUpdateBidIfHigher(long auctionId, double amount, String bidderUsername, long nowEpoch) {
        int rows = jdbc.update(
                "UPDATE auctions " +
                        "SET current_highest_bid = ?, highest_bidder_username = ? " +
                        "WHERE id = ? " +
                        "  AND status = 'ACTIVE' " +
                        "  AND (created_at_epoch + duration_seconds) > ? " +
                        "  AND ? > MAX(current_highest_bid, starting_price)",
                amount, bidderUsername, auctionId, nowEpoch, amount
        );
        return rows > 0;
    }

    public void endAuction(long auctionId, String result) {
        jdbc.update("UPDATE auctions SET status = 'ENDED', result = ? WHERE id = ?", result, auctionId);
    }

    public record AuctionRecord(
            long id, long itemId, String sellerUsername, double startingPrice,
            double currentHighestBid, String highestBidderUsername,
            String status, String result, long createdAtEpoch, long durationSeconds
    ) {
        public long remainingSeconds() {
            long end = createdAtEpoch + durationSeconds;
            long now = System.currentTimeMillis() / 1000;
            return Math.max(0, end - now);
        }
    }
}
