package com.forwardauction.iam.dto;

public record UserProfileResponse(
        long userId,
        String username,
        String firstName,
        String lastName,
        String address
) {}
