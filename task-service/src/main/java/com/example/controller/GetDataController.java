package com.example.controller;

import com.example.enums.Setting;
import com.example.task.GetNFTCollectionItemsValueTask;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class GetDataController {

    private final GetNFTCollectionItemsValueTask task;

    public GetDataController(GetNFTCollectionItemsValueTask task) {
        this.task = task;
    }
    /**
     * 取得當前的列表
     */
    @PostMapping("/getList")
    public Map<String, Object> manualTrigger() {
        Setting.GLOBAL_LOGGER.info("[manualTrigger]");
        return task.executeGetListTask();
    }
}
