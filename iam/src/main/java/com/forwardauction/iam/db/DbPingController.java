package com.forwardauction.iam.db;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbPingController {

    private final JdbcTemplate jdbc;

    public DbPingController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/db-health")
    public Map<String, Object> dbHealth() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return Map.of("status", "UP", "usersCount", n);
    }
}