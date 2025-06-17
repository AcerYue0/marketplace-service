package com.example.controller;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.enums.Setting;
import com.example.service.MarketplaceService;

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
            Setting.GLOBAL_LOGGER.info("[/api/marketplace/fetchList] Fetching marketplace data");
            marketplaceService.fetchAndSaveLowestPrices();
            return ResponseEntity.ok("[/api/marketplace/fetchList] Fetched and saved successfully.");
        } catch (Exception e) {
            Setting.GLOBAL_LOGGER.info("[/api/marketplace/fetchList] ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("[Marketplace] Error: " + e.getMessage());
        }
    }

    @GetMapping("/getList")
    public ResponseEntity<?> getMarketplaceList() {
        try {
            return ResponseEntity.ok(marketplaceService.loadLatestResult());
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to load marketplace data.");
        }
    }
}
