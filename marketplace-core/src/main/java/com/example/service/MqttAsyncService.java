package com.example.service;

import com.example.enums.Setting;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MqttAsyncService {

    private final MqttSenderService mqttSenderService;

    public MqttAsyncService(MqttSenderService mqttSenderService) {
        this.mqttSenderService = mqttSenderService;
    }

    @Async
    public void sendPriceUpdate(String name, BigDecimal price, Long categoryNo, String imgUrl) {
        try {
            String payload = String.format(
                "{\"%s\": {\"price\": %s, \"categoryNo\": %d, \"imgUrl\": %s}}",
                name, price.toPlainString(), categoryNo, imgUrl
            );
            mqttSenderService.sendMessage(payload);
        } catch (Exception e) {
            // 非同步失敗不影響主流程
            Setting.GLOBAL_LOGGER.error("MQTT async send failed: {}", e.getMessage());
        }
    }
}
