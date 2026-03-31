package com.forwardauction.payment.service;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.forwardauction.payment.dto.PaymentRequest;
import com.forwardauction.payment.dto.PaymentResponse;
import com.forwardauction.payment.repo.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final ConcurrentHashMap<Long, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    public PaymentService(PaymentRepository payments) {
        this.payments = payments;
    }

    public PaymentResponse processPayment(PaymentRequest req) {
        validateCard(req.cardNumber(), req.expiryDate(), req.securityCode());

        String cardLastFour = req.cardNumber().substring(req.cardNumber().length() - 4);

        double total = req.itemPrice() + req.shippingCost();
        if (req.expedited()) {
            total += req.expeditedShippingCost();
        }

        ReentrantLock lock = auctionLocks.computeIfAbsent(req.auctionId(), id -> new ReentrantLock());
        lock.lock();
        try {
            long paymentId = payments.create(
                    req.auctionId(), req.itemId(), req.winnerUsername(), req.itemTitle(),
                    req.itemPrice(), req.shippingCost(), req.expeditedShippingCost(),
                    total, req.cardName(), cardLastFour,
                    req.shippingDays(), req.expedited()
            );

            return payments.findById(paymentId)
                    .orElseThrow(() -> new IllegalStateException("Payment created but not found"));
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                auctionLocks.remove(req.auctionId(), lock);
            }
        }
    }

    public PaymentResponse getPayment(long paymentId) {
        return payments.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));
    }

    public PaymentResponse getPaymentByAuction(long auctionId) {
        return payments.findByAuctionId(auctionId)
                .orElseThrow(() -> new NoSuchElementException("No payment found for auction: " + auctionId));
    }

    private void validateCard(String cardNumber, String expiryDate, String securityCode) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException("Card number must be exactly 16 digits");
        }
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("Expiry date must be in MM/YY format");
        }
        if (securityCode == null || !securityCode.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Security code must be 3 or 4 digits");
        }
    }
}
