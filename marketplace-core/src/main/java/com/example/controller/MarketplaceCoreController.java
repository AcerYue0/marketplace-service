package com.example.controller;

import com.example.service.MarketplaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceCoreController {

    private final MarketplaceService marketplaceService;

    public MarketplaceCoreController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostMapping("/fetchList")
    public ResponseEntity<String> runTask() {
        try {
            marketplaceService.fetchAndSaveLowestPrices();
            return ResponseEntity.ok("[Marketplace] Fetched and saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("[Marketplace] Error: " + e.getMessage());
        }
    }
}