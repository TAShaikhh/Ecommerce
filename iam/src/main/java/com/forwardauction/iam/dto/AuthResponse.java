package com.forwardauction.iam.dto;

public record AuthResponse(
        long userId,
        String username
) {}