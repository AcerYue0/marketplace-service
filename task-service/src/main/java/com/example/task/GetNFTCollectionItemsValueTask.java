package com.example.task;

import com.example.enums.Setting;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GetNFTCollectionItemsValueTask {
    private final RestTemplate restTemplate = new RestTemplate();

//    @Scheduled(cron = "0 */30 * * * *")
//    public void runScheduledTask() {
//        Setting.GLOBAL_LOGGER.info("[runScheduledTask]");
//        executeGetListTask();
//    }

    public Map<String, Object> executeGetListTask() {
        Setting.GLOBAL_LOGGER.info("[executeGetListTask]");
        Map<String, Object> list = null;
        ResponseEntity<?> response;

        try {
            response = restTemplate.getForEntity(
                "https://marketplace-core-ll9s.onrender.com/api/marketplace/getList",
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String json = (String) response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                list = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            } else {
                Setting.GLOBAL_LOGGER.warn("Failed to get valid response: " + response.getStatusCode());
            }
        } catch (Exception e) {
            Setting.GLOBAL_LOGGER.error("Exception occurred during executeGetListTask: ", e);
        }

        return list;
    }
}
