package com.example.controller;

import com.example.enums.Setting;
import com.example.task.FetchAllNFTCollectionItemsTask;
import com.example.service.TimerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TimerService timerService;
    private final FetchAllNFTCollectionItemsTask task;

    public TaskController(TimerService timerService, FetchAllNFTCollectionItemsTask task) {
        this.timerService = timerService;
        this.task = task;
    }

    /**
     * 手動觸發任務一次，並關閉 cron 執行
     */
    @PostMapping("/trigger")
    public String manualTrigger() {
        Setting.GLOBAL_LOGGER.trace("[manualTrigger]");
        if (!task.isCronEnabled()) {
            return "Too many requests. Please wait after last execution finished.";
        }
        task.disableCron();
        timerService.start20MinTimer(task); // 非同步排程恢復 cron
        task.executeFetchTask(); // 手動執行任務
        return "Manual task executed. Cron disabled for 20 minutes.";
    }

    /**
     * 啟用 cron 排程
     */
    @PostMapping("/cronEnable")
    public String enableCron() {
        Setting.GLOBAL_LOGGER.trace("[enableCron]");
        task.enableCron();
        return "Cron enabled.";
    }

    /**
     * 禁用 cron 排程
     */
    @PostMapping("/cronDisable")
    public String disableCron() {
        Setting.GLOBAL_LOGGER.trace("[disableCron]");
        task.disableCron();
        return "Cron disabled.";
    }

    /**
     * 查詢 cron 是否開啟
     */
    @GetMapping("/cronStatus")
    public String cronStatus() {
        Setting.GLOBAL_LOGGER.trace("[cronStatus]");
        return task.isCronEnabled() ? "Cron is ENABLED." : "Cron is DISABLED.";
    }
}
