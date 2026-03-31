package com.forwardauction.catalogue.dto;

public record ItemDetailResponse(
        long id,
        String ownerUsername,
        String title,
        String description,
        String condition,
        String keywords,
        double shippingCost,
        double expeditedShippingCost,
        int shippingDays,
        String imageUrl,
        String status,
        long createdAtEpoch
) {}
