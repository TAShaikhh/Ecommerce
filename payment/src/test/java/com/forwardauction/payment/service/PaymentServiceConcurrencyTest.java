package com.forwardauction.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.forwardauction.payment.dto.PaymentRequest;

@SpringBootTest
class PaymentServiceConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void processPayment_sameAuction_concurrent_onlyOneSucceeds() throws Exception {
        long auctionId = 88001L;
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<String> task = () -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                try {
                    paymentService.processPayment(requestFor(auctionId));
                    return "OK";
                } catch (Exception ex) {
                    return ex.getClass().getSimpleName() + ":" + ex.getMessage();
                }
            };

            Future<String> f1 = pool.submit(task);
            Future<String> f2 = pool.submit(task);

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();

            List<String> outcomes = new ArrayList<>();
            outcomes.add(f1.get(5, TimeUnit.SECONDS));
            outcomes.add(f2.get(5, TimeUnit.SECONDS));

            long okCount = outcomes.stream().filter("OK"::equals).count();
            long conflictCount = outcomes.stream().filter(o -> o.startsWith("IllegalStateException:")).count();

            assertEquals(1, okCount, "Exactly one payment should succeed for the same auction");
            assertEquals(1, conflictCount, "The second concurrent payment should be rejected");
            assertTrue(outcomes.stream().anyMatch(o -> o.contains("Payment already exists")));
        } finally {
            pool.shutdownNow();
        }
    }

    private PaymentRequest requestFor(long auctionId) {
        return new PaymentRequest(
                auctionId,
                auctionId,
                "buyer-race",
                "Race Item",
                100.0,
                10.0,
                0.0,
                110.0,
                "Buyer Race",
                "4111111111111111",
                "12/29",
                "123",
                5,
                false
        );
    }
}
