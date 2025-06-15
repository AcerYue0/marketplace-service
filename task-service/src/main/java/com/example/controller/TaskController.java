package com.example.controller;

import com.example.service.FetchAllNFTCollectionItemsTask;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final FetchAllNFTCollectionItemsTask taskService;

    public TaskController(FetchAllNFTCollectionItemsTask taskService) {
        this.taskService = taskService;
    }

    /**
     * 手動觸發任務一次，並關閉 cron 執行
     */
    @PostMapping("/trigger")
    public String manualTrigger() {
        if (!taskService.isCronEnabled()) {
            return "Too many request. Please wait after last execution finished.";
        }
        taskService.disableCron();
        // TODO taskService.startCronCooldownTimer();
        taskService.executeFetchTask();
        return "Manual task executed. Cron disabled.";
    }

    /**
     * 啟用 cron 排程
     */
    @PostMapping("/cronEnable")
    public String enableCron() {
        taskService.enableCron();
        return "Cron enabled.";
    }

    /**
     * 禁用 cron 排程
     */
    @PostMapping("/cronDisable")
    public String disableCron() {
        taskService.disableCron();
        return "Cron disabled.";
    }

    /**
     * 查詢 cron 是否開啟
     */
    @GetMapping("/cronStatus")
    public String cronStatus() {
        return taskService.isCronEnabled() ? "Cron is ENABLED." : "Cron is DISABLED.";
    }
}