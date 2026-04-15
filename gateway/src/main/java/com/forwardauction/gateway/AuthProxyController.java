package com.forwardauction.gateway;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.forwardauction.gateway.session.SessionStore;
import com.forwardauction.gateway.util.HateoasHelper;

@RestController
@RequestMapping("/auth")
public class AuthProxyController {

    private final RestTemplate restTemplate;
    private final SessionStore sessionStore;

    @Value("${services.iamBaseUrl}")
    private String iamBaseUrl;

    public AuthProxyController(RestTemplate restTemplate, SessionStore sessionStore) {
        this.restTemplate = restTemplate;
        this.sessionStore = sessionStore;
    }

    @PostMapping("/signup")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Object body) {
        Map<String, Object> iamRes = restTemplate.postForObject(iamBaseUrl + "/auth/signup", body, Map.class);

        Map<String, Object> result = new LinkedHashMap<>(iamRes);
        result.put("_links", HateoasHelper.links()
                .add("self", "/auth/signup")
                .add("login", "/auth/login")
                .build());

        return ResponseEntity.status(201).body(result);
    }

    @PostMapping("/login")
    @SuppressWarnings("unchecked")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        Map<String, Object> iamRes = restTemplate.postForObject(iamBaseUrl + "/auth/login", body, Map.class);

        long userId = ((Number) iamRes.get("userId")).longValue();
        String username = (String) iamRes.get("username");
        String token = sessionStore.createSession(userId, username);

        Map<String, Object> result = new LinkedHashMap<>(iamRes);
        result.put("token", token);
        result.put("_links", HateoasHelper.links()
                .add("self", "/auth/login")
                .add("catalogue", "/catalogue")
                .add("createItem", "/items")
                .add("me", "/auth/me")
                .add("logout", "/auth/logout")
                .build());

        return result;
    }

    @PostMapping("/reset-password")
    @SuppressWarnings("unchecked")
    public Map<String, Object> resetPassword(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid or missing session token"));

        Object rawUsername = body.get("username");
        if (rawUsername == null) {
            throw new IllegalArgumentException("username is required");
        }
        String requestedUsername = String.valueOf(rawUsername).trim();
        if (requestedUsername.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (!session.username().equals(requestedUsername)) {
            throw new SecurityException("You can only reset your own password");
        }

        Map<String, Object> iamRes = restTemplate.postForObject(iamBaseUrl + "/auth/reset-password", body, Map.class);

        Map<String, Object> result = new LinkedHashMap<>(iamRes);
        result.put("_links", HateoasHelper.links()
                .add("self", "/auth/reset-password")
                .add("login", "/auth/login")
                .build());
        return result;
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        var session = sessionStore.getSession(token)
                .orElseThrow(() -> new SecurityException("Invalid or missing session token"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", session.userId());
        result.put("username", session.username());
        result.put("selectedItemId", session.selectedItemId());
        result.put("_links", HateoasHelper.links()
                .add("self", "/auth/me")
                .add("catalogue", "/catalogue")
                .add("logout", "/auth/logout")
                .build());

        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        sessionStore.invalidate(token);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("message", "Logged out successfully");
        result.put("_links", HateoasHelper.links()
                .add("self", "/auth/logout")
                .add("login", "/auth/login")
                .build());
        return result;
    }

    static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new SecurityException("Missing bearer token");
        }
        if (token.startsWith("<") && token.endsWith(">")) {
            throw new SecurityException("Replace the <token> placeholder with the token returned by POST /auth/login");
        }
        return token;
    }
}
