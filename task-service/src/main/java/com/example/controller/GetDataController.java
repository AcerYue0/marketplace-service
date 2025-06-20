package com.example.controller;

import com.example.enums.Setting;
import com.example.task.GetNFTCollectionItemsValueTask;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
public class GetDataController {

    private GetNFTCollectionItemsValueTask task;

    public GetDataController(GetNFTCollectionItemsValueTask task) {
        this.task = task;
    }
    /**
     * 取得當前的列表
     */
    @PostMapping("/getList")
    public String manualTrigger() {
        Setting.GLOBAL_LOGGER.info("[manualTrigger]");
        task.executeGetListTask();
        return "Get all item price in NFT collections complete.";
    }
}
