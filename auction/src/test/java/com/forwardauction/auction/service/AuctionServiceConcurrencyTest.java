package com.forwardauction.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.forwardauction.auction.repo.AuctionRepository;

@SpringBootTest
class AuctionServiceConcurrencyTest {

    @Autowired
    private AuctionRepository auctions;

    @Autowired
    private AuctionService auctionService;

    @Test
    void tryUpdateBidIfHigher_lowerCannotOverwriteHigher() {
        long auctionId = auctions.create(98001L, "seller-race", 100.0, 300L);
        long now = System.currentTimeMillis() / 1000;

        assertTrue(auctions.tryUpdateBidIfHigher(auctionId, 150.0, "buyer-high", now));
        assertFalse(auctions.tryUpdateBidIfHigher(auctionId, 120.0, "buyer-low", now));

        var state = auctionService.getAuction(auctionId);
        assertEquals(150.0, state.currentHighestBid());
        assertEquals("buyer-high", state.highestBidderUsername());
    }

    @Test
    void placeBid_concurrentLowerAndHigher_finalHighestIsNotOverwritten() throws Exception {
        long auctionId = auctions.create(98002L, "seller-race", 100.0, 300L);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> lowBid = () -> submitBid(auctionId, "buyer-low", 120, ready, start);
            Callable<Integer> highBid = () -> submitBid(auctionId, "buyer-high", 150, ready, start);

            Future<Integer> lowResult = pool.submit(lowBid);
            Future<Integer> highResult = pool.submit(highBid);

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();

            Integer lowAccepted = lowResult.get(5, TimeUnit.SECONDS);
            Integer highAccepted = highResult.get(5, TimeUnit.SECONDS);

            var state = auctionService.getAuction(auctionId);

            int expectedHighest = 0;
            if (lowAccepted != null) {
                expectedHighest = Math.max(expectedHighest, lowAccepted);
            }
            if (highAccepted != null) {
                expectedHighest = Math.max(expectedHighest, highAccepted);
            }

            assertEquals((double) expectedHighest, state.currentHighestBid());
            if (expectedHighest == 150) {
                assertEquals("buyer-high", state.highestBidderUsername());
            } else if (expectedHighest == 120) {
                assertEquals("buyer-low", state.highestBidderUsername());
            } else {
                assertTrue(state.highestBidderUsername() == null);
            }
            assertTrue(expectedHighest == 0 || expectedHighest == 120 || expectedHighest == 150);
        } finally {
            pool.shutdownNow();
        }
    }

    private Integer submitBid(long auctionId, String bidder, int amount,
                              CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            start.await(5, TimeUnit.SECONDS);
            auctionService.placeBid(auctionId, bidder, amount);
            return amount;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}
