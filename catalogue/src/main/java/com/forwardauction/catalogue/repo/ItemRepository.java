package com.forwardauction.catalogue.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.forwardauction.catalogue.dto.ItemDetailResponse;

@Repository
public class ItemRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<ItemDetailResponse> ROW_MAPPER = (rs, rowNum) -> new ItemDetailResponse(
            rs.getLong("id"),
            rs.getString("owner_username"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("condition"),
            rs.getString("keywords"),
            rs.getDouble("shipping_cost"),
            rs.getDouble("expedited_shipping_cost"),
            rs.getInt("shipping_days"),
            rs.getString("image_url"),
            rs.getString("status"),
            rs.getLong("created_at_epoch")
    );

    public ItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long create(String ownerUsername, String title, String description, String condition,
                       String keywords, double shippingCost, double expeditedShippingCost,
                       int shippingDays, String imageUrl) {
        long now = System.currentTimeMillis() / 1000;
        jdbc.update(
                "INSERT INTO items(owner_username, title, description, condition, keywords, shipping_cost, expedited_shipping_cost, shipping_days, image_url, status, created_at_epoch) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                ownerUsername, title, description, condition,
                keywords == null ? "" : keywords,
                shippingCost, expeditedShippingCost, shippingDays,
                imageUrl == null ? "" : imageUrl,
                "ACTIVE", now
        );
        Long id = jdbc.queryForObject(
                "SELECT id FROM items WHERE owner_username=? AND title=? ORDER BY id DESC LIMIT 1",
                Long.class, ownerUsername, title
        );
        return id == null ? -1 : id;
    }

    public Optional<ItemDetailResponse> findById(long id) {
        return jdbc.query("SELECT * FROM items WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public List<ItemDetailResponse> findActive() {
        return jdbc.query("SELECT * FROM items WHERE status = 'ACTIVE' ORDER BY created_at_epoch DESC", ROW_MAPPER);
    }

    public List<ItemDetailResponse> searchByKeyword(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return jdbc.query(
                "SELECT * FROM items WHERE status = 'ACTIVE' AND (LOWER(title) LIKE ? OR LOWER(keywords) LIKE ?) ORDER BY created_at_epoch DESC",
                ROW_MAPPER, like, like
        );
    }

    public void updateStatus(long id, String status) {
        int rows = jdbc.update("UPDATE items SET status = ? WHERE id = ?", status, id);
        if (rows == 0) {
            throw new java.util.NoSuchElementException("Item not found: " + id);
        }
    }
}
