package com.example.service;

import com.example.task.FetchAllNFTCollectionItemsTask;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
public class TimerService {

    @Async
    public void start15MinTimer(FetchAllNFTCollectionItemsTask task) {
        try {
            Thread.sleep(15 * 60 * 1000); // 等待15分鐘
            task.enableCron();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
