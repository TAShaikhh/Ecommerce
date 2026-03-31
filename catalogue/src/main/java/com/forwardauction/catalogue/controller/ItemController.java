package com.forwardauction.catalogue.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.forwardauction.catalogue.dto.CreateItemRequest;
import com.forwardauction.catalogue.dto.CreateItemResponse;
import com.forwardauction.catalogue.dto.ItemDetailResponse;
import com.forwardauction.catalogue.repo.ItemRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemRepository items;

    public ItemController(ItemRepository items) {
        this.items = items;
    }

    @PostMapping
    public ResponseEntity<CreateItemResponse> create(@Valid @RequestBody CreateItemRequest req) {
        long itemId = items.create(
                req.ownerUsername(), req.title(), req.description(), req.condition(),
                req.keywords(), req.shippingCost(), req.expeditedShippingCost(),
                req.shippingDays(), req.imageUrl()
        );
        return ResponseEntity.status(201).body(new CreateItemResponse(itemId, req.title(), "ACTIVE"));
    }

    @GetMapping("/{id}")
    public ItemDetailResponse getById(@PathVariable long id) {
        return items.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found: " + id));
    }

    @GetMapping
    public List<ItemDetailResponse> search(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return items.searchByKeyword(keyword);
        }
        return items.findActive();
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        items.updateStatus(id, status);
        return Map.of("itemId", id, "status", status);
    }
}
