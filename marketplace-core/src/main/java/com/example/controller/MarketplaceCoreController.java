package com.example.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.enums.Setting;
import com.example.service.MarketplaceService;

import static com.example.enums.Setting.OUTPUT_FILE;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceCoreController {

    private final MarketplaceService marketplaceService;

    public MarketplaceCoreController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostMapping("/fetchList")
    public ResponseEntity<String> fetchList() {
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

    @PostMapping("/saveList")
    public ResponseEntity<String> saveList(@RequestBody String jsonPayload) {
        try {
            Setting.GLOBAL_LOGGER.info("[/api/marketplace/saveList] Saving marketplace data");

            // 確保目錄存在
            File parentDir = OUTPUT_FILE.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directories for " + parentDir.getAbsolutePath());
            }

            // 寫入 JSON 到檔案
            try (FileWriter writer = new FileWriter(OUTPUT_FILE)) {
                writer.write(jsonPayload);
            }

            return ResponseEntity.ok("[/api/marketplace/saveList] Saved successfully.");
        } catch (Exception e) {
            Setting.GLOBAL_LOGGER.error("[/api/marketplace/saveList] Error saving data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("[Marketplace] Error: " + e.getMessage());
        }
    }

    @GetMapping("/getList")
    public ResponseEntity<?> getMarketplaceList() {
        try {
            Setting.GLOBAL_LOGGER.info("[/api/marketplace/getList] Getting price data");
            return ResponseEntity.ok(marketplaceService.loadLatestResult());
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to load price data.");
        }
    }
}
