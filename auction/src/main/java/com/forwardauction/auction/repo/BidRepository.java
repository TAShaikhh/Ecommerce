package com.forwardauction.auction.repo;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BidRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<BidRecord> ROW_MAPPER = (rs, rowNum) -> new BidRecord(
            rs.getLong("id"),
            rs.getLong("auction_id"),
            rs.getString("bidder_username"),
            rs.getInt("amount"),
            rs.getLong("placed_at_epoch")
    );

    public BidRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long create(long auctionId, String bidderUsername, int amount) {
        long now = System.currentTimeMillis() / 1000;
        jdbc.update(
                "INSERT INTO bids(auction_id, bidder_username, amount, placed_at_epoch) VALUES(?,?,?,?)",
                auctionId, bidderUsername, amount, now
        );
        Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
        return id == null ? -1 : id;
    }

    public List<BidRecord> findByAuctionId(long auctionId) {
        return jdbc.query(
                "SELECT * FROM bids WHERE auction_id = ? ORDER BY placed_at_epoch DESC",
                ROW_MAPPER, auctionId
        );
    }

    public record BidRecord(long id, long auctionId, String bidderUsername, int amount, long placedAtEpoch) {}
}
