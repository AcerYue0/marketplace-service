package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.FetchAllNFTCollectionItemsTask;
import com.example.service.TimerService;

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
        if (!task.isCronEnabled()) {
            return "Too many requests. Please wait after last execution finished.";
        }
        task.disableCron();
        timerService.start15MinTimer(task); // 非同步排程恢復 cron
        task.executeFetchTask(); // 手動執行任務
        return "Manual task executed. Cron disabled for 15 minutes.";
    }

    // TODO add getlist to call /api/marketplace/getList

    /**
     * 啟用 cron 排程
     */
    @PostMapping("/cronEnable")
    public String enableCron() {
        task.enableCron();
        return "Cron enabled.";
    }

    /**
     * 禁用 cron 排程
     */
    @PostMapping("/cronDisable")
    public String disableCron() {
        task.disableCron();
        return "Cron disabled.";
    }

    /**
     * 查詢 cron 是否開啟
     */
    @GetMapping("/cronStatus")
    public String cronStatus() {
        return task.isCronEnabled() ? "Cron is ENABLED." : "Cron is DISABLED.";
    }
}
