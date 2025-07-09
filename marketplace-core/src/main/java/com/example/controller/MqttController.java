package com.example.controller;

import com.example.service.MqttSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class MqttController {

    @Autowired
    private MqttSenderService mqttSenderService;

    @PostMapping("/send")
    public String send(@RequestParam String msg) {
        mqttSenderService.sendMessage(msg);
        return "Message sent: " + msg;
    }
}
