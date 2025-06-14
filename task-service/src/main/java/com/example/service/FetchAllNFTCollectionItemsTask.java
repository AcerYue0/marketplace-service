package com.example.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FetchAllNFTCollectionItemsTask {

    private final RestTemplate restTemplate = new RestTemplate();

    // 可透過前端或手動API切換flag(暫用靜態變數)
    private static volatile boolean cronEnabled = true;

    @Scheduled(cron = "0 */15 * * * *")
    public void runScheduledTask() {
        //TODO Log
        if (!cronEnabled) {
            return;
        }
        executeFetchTask();
    }

    private void executeFetchTask() {
        try {
            restTemplate.postForEntity("http://localhost:8081/api/marketplace/fetchList", null, String.class);
        } catch (Exception e) {
            // TODO Log
        }
    }

    public void disableCron() {
        cronEnabled = false;
    }

    public void enableCron() {
        cronEnabled = true;
    }

    public boolean isCronEnabled() {
        return cronEnabled;
    }
}