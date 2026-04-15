package com.forwardauction.payment.dto;

public record PaymentResponse(
        long paymentId,
        long auctionId,
        long itemId,
        String winnerUsername,
        String itemTitle,
        double itemPrice,
        double shippingCost,
        double expeditedShippingCost,
        double totalPaid,
        String cardName,
        String cardLastFour,
        int shippingDays,
        boolean expedited,
        String status,
        long createdAtEpoch
) {}
