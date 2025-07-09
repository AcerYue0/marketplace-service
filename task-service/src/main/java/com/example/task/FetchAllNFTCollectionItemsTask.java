package com.example.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.enums.Setting;

@Service
public class FetchAllNFTCollectionItemsTask {

    private final RestTemplate restTemplate = new RestTemplate();

    // 可透過前端或手動API切換flag(暫用靜態變數)
    private static volatile boolean cronEnabled = true;
    private static volatile boolean isSkiped = false;

    @Scheduled(cron = "0 */20 * * * *")
    public void runScheduledTask() {
        Setting.GLOBAL_LOGGER.info("[runScheduledTask]");
        if (!cronEnabled) {
            isSkiped = true;
            Setting.GLOBAL_LOGGER.info("[runScheduledTask] Cron disabled.");
            return;
        }
        executeFetchTask();
        if (isSkiped) {
            enableCron();
        }
    }

    public void executeFetchTask() {
        Setting.GLOBAL_LOGGER.info("[executeFetchTask]");
        try {
            restTemplate.postForEntity("http://127.0.0.1:8082/api/marketplace/fetchList", null, String.class);
        } catch (Exception e) {
            Setting.GLOBAL_LOGGER.info(String.valueOf(e));
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
