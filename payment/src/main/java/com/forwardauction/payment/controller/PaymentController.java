package com.forwardauction.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.forwardauction.payment.dto.PaymentRequest;
import com.forwardauction.payment.dto.PaymentResponse;
import com.forwardauction.payment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest req) {
        PaymentResponse resp = paymentService.processPayment(req);
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getById(@PathVariable long paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @GetMapping("/by-auction/{auctionId}")
    public PaymentResponse getByAuction(@PathVariable long auctionId) {
        return paymentService.getPaymentByAuction(auctionId);
    }
}
