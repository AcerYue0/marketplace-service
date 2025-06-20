package com.example.task;

import com.example.enums.Setting;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

public class GetNFTCollectionItemsValueTask {
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 */30 * * * *")
    public void runScheduledTask() {
        Setting.GLOBAL_LOGGER.info("[runScheduledTask]");
        executeGetListTask();
    }

    public void executeGetListTask() {
        Setting.GLOBAL_LOGGER.info("[executeGetListTask]");
        try {
            restTemplate.postForEntity("http://localhost:8082/api/marketplace/getList", null, String.class);
        } catch (Exception e) {
            Setting.GLOBAL_LOGGER.info(String.valueOf(e));
        }
    }
}
