package com.forwardauction.auction.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.forwardauction.auction.dto.AuctionStateResponse;
import com.forwardauction.auction.dto.BidRequest;
import com.forwardauction.auction.dto.BidResponse;
import com.forwardauction.auction.dto.CreateAuctionRequest;
import com.forwardauction.auction.dto.CreateAuctionResponse;
import com.forwardauction.auction.repo.AuctionRepository;
import com.forwardauction.auction.repo.AuctionRepository.AuctionRecord;
import com.forwardauction.auction.repo.BidRepository.BidRecord;
import com.forwardauction.auction.service.AuctionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    private final AuctionRepository auctionRepo;
    private final AuctionService auctionService;

    public AuctionController(AuctionRepository auctionRepo, AuctionService auctionService) {
        this.auctionRepo = auctionRepo;
        this.auctionService = auctionService;
    }

    @PostMapping
    public ResponseEntity<CreateAuctionResponse> create(@Valid @RequestBody CreateAuctionRequest req) {
        long auctionId = auctionRepo.create(req.itemId(), req.sellerUsername(), req.startingPrice(), req.durationSeconds());
        return ResponseEntity.status(201).body(new CreateAuctionResponse(auctionId, "ACTIVE"));
    }

    @GetMapping("/{auctionId}")
    public AuctionStateResponse getAuction(@PathVariable long auctionId) {
        AuctionRecord a = auctionService.getAuction(auctionId);
        return toState(a);
    }

    @GetMapping("/by-item/{itemId}")
    public AuctionStateResponse getByItem(@PathVariable long itemId) {
        AuctionRecord a = auctionService.getAuctionByItemId(itemId);
        return toState(a);
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<BidResponse> placeBid(@PathVariable long auctionId,
                                                @Valid @RequestBody BidRequest req) {
        BidRecord bid = auctionService.placeBid(auctionId, req.bidderUsername(), req.amount());
        AuctionRecord updated = auctionService.getAuction(auctionId);
        List<BidRecord> history = auctionService.getBidHistory(auctionId);

        BidResponse resp = new BidResponse(
                bid.id(), auctionId, bid.bidderUsername(), bid.amount(),
                updated.currentHighestBid(), updated.highestBidderUsername(),
                updated.remainingSeconds(), history.size()
        );
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/{auctionId}/bids")
    public List<BidRecord> getBidHistory(@PathVariable long auctionId) {
        return auctionService.getBidHistory(auctionId);
    }

    @GetMapping("/{auctionId}/result")
    public AuctionStateResponse getResult(@PathVariable long auctionId) {
        AuctionRecord a = auctionService.getAuction(auctionId);
        if (!"ENDED".equals(a.status())) {
            throw new IllegalStateException("Auction has not ended yet");
        }
        return toState(a);
    }

    private AuctionStateResponse toState(AuctionRecord a) {
        return new AuctionStateResponse(
                a.id(), a.itemId(), a.sellerUsername(), a.startingPrice(),
                a.currentHighestBid(), a.highestBidderUsername(),
                a.status(), a.result(), a.remainingSeconds()
        );
    }
}
