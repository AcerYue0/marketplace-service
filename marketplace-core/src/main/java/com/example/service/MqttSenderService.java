package com.example.service;

import com.example.enums.Setting;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
public class MqttSenderService {

    private final String brokerUrl = "tcp://127.0.0.1:1883"; // 注意不是 WebSocket
    private final String topic = "marketplace/item/update";

    public void sendMessage(String payload) {
        try {
            String clientId = MqttClient.generateClientId();
            MqttClient client = new MqttClient(brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);

            client.publish(topic, message);
            Setting.GLOBAL_LOGGER.info("[sendMessage] MQTT send message: {}", payload);
            Thread.sleep(100L);
            Setting.GLOBAL_LOGGER.info("[sendMessage] MQTT message sent");
            client.disconnect();
        } catch (MqttException | InterruptedException e) {
            Setting.GLOBAL_LOGGER.error(e.getMessage());
        }
    }
}
