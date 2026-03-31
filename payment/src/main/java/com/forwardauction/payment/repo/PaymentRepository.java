package com.forwardauction.payment.repo;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.forwardauction.payment.dto.PaymentResponse;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<PaymentResponse> ROW_MAPPER = (rs, rowNum) -> new PaymentResponse(
            rs.getLong("id"),
            rs.getLong("auction_id"),
            rs.getLong("item_id"),
            rs.getString("winner_username"),
            rs.getString("item_title"),
            rs.getDouble("item_price"),
            rs.getDouble("shipping_cost"),
            rs.getDouble("expedited_shipping_cost"),
            rs.getDouble("total_paid"),
            rs.getString("card_name"),
            rs.getString("card_last_four"),
            rs.getInt("shipping_days"),
            rs.getInt("expedited") == 1,
            rs.getString("status"),
            rs.getLong("created_at_epoch")
    );

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long create(long auctionId, long itemId, String winnerUsername, String itemTitle,
                       double itemPrice, double shippingCost, double expeditedShippingCost,
                       double totalPaid, String cardName, String cardLastFour,
                       int shippingDays, boolean expedited) {
        long now = System.currentTimeMillis() / 1000;
        int rows;
        try {
            rows = jdbc.update(
                    "INSERT INTO payments(auction_id, item_id, winner_username, item_title, item_price, shipping_cost, expedited_shipping_cost, total_paid, card_name, card_last_four, shipping_days, expedited, status, created_at_epoch) " +
                            "SELECT ?,?,?,?,?,?,?,?,?,?,?,?,?,? " +
                            "WHERE NOT EXISTS (SELECT 1 FROM payments WHERE auction_id = ?)",
                    auctionId, itemId, winnerUsername, itemTitle, itemPrice, shippingCost,
                    expeditedShippingCost, totalPaid, cardName, cardLastFour,
                    shippingDays, expedited ? 1 : 0, "COMPLETED", now,
                    auctionId
            );
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Payment already exists for auction: " + auctionId);
        }
        if (rows == 0) {
            throw new IllegalStateException("Payment already exists for auction: " + auctionId);
        }
        Long id = jdbc.queryForObject("SELECT id FROM payments WHERE auction_id = ?", Long.class, auctionId);
        return id == null ? -1 : id;
    }

    public Optional<PaymentResponse> findById(long id) {
        return jdbc.query("SELECT * FROM payments WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public Optional<PaymentResponse> findByAuctionId(long auctionId) {
        return jdbc.query("SELECT * FROM payments WHERE auction_id = ?", ROW_MAPPER, auctionId)
                .stream().findFirst();
    }
}
