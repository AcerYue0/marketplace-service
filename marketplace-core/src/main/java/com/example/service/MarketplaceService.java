package com.example.service;

import com.example.enums.Setting;
import com.example.util.MarketplaceItemDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class MarketplaceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public void fetchAndSaveLowestPrices() throws IOException, InterruptedException {
        Setting.GLOBAL_LOGGER.trace("[fetchAndSaveLowestPrices]");

        InputStream input = getClass().getClassLoader().getResourceAsStream(Setting.CLASSIFICATION_FILE);
        if (input == null) {
            Setting.GLOBAL_LOGGER.info("Resource not found: " + Setting.CLASSIFICATION_FILE);
        }
        Map<String, List<String>> keywordMap = mapper.readValue(input, new TypeReference<>() {});
        // 取出 "others" 的值並轉換為 List<String>
        List<String> others = keywordMap.remove("others"); // 先移除原本的 "others" 鍵，避免干擾

        if (others != null) {
            for (String item : others) {
                // 為每個 item 加入一筆新的 key-value
                keywordMap.put(item, Collections.singletonList(item));
            }
        }

        Map<String, BigDecimal> results = new HashMap<>();
        // 若已有 output.json，先載入舊資料
        if (Setting.OUTPUT_FILE.exists()) {
            try {
                List<Map<String, Object>> oldList = mapper.readValue(Setting.OUTPUT_FILE,
                        new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> entry : oldList) {
                    String name = (String) entry.get("name");
                    BigDecimal price = (BigDecimal) entry.get("price");
                    results.put(name, price);
                }
            } catch (IOException e) {
                Setting.GLOBAL_LOGGER.info("Failed to read existing output.json, starting fresh.");
            }
        }

        // 根據關鍵字列表模糊搜尋
        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            String keyword = entry.getKey(); // 每個keyword進行搜尋
            List<String> values = entry.getValue(); // keyword中對應的物件

            // 初始化迴圈值
            int pageNo = 1;
            boolean hasMorePage = true;
            Set<String> foundValues = new HashSet<>();

            while (hasMorePage) {
                Map<String, Object> body = createRequestBody(keyword, pageNo);
                HttpHeaders header = createHeaders();
                HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), header);

                Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] start fetching item: {}, {}", header, body);
                ResponseEntity<Map> response = restTemplate.exchange(Setting.API_URL, HttpMethod.POST, request, Map.class);

                // TODO 429 status code handler

                // 每次請求後停止 4 秒
                Thread.sleep(4000);

                // response解析
                Map<String, Object> responseMap = (Map<String, Object>) response.getBody();
                if (responseMap == null) {
                    Setting.GLOBAL_LOGGER.error("[fetchAndSaveLowestPrices] fetch complete but response not properly: {}", response);
                    break;
                }
                List<Map<String, Object>> items = (List<Map<String, Object>>) responseMap.get("items");
                Map<String, Object> pagination = (Map<String, Object>) responseMap.get("paginationResult");
                // 判斷總物品數量
                if ((int) pagination.get("totalCount") == 0) {
                    Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] fetch complete but no item found: {}", response);
                    break;
                }

                Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] fetch complete and item found, row response: {}", response);

                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String name = (String) item.get("name");
                        if (values.stream().anyMatch(name::contains)) {
                            Map<String, Object> salesInfo = (Map<String, Object>) item.get("salesInfo");
                            String priceWei = (String) salesInfo.get("priceWei");
                            if (priceWei == null || priceWei.isEmpty()) continue;
                            BigDecimal price = new BigDecimal(priceWei).divide(BigDecimal.TEN.pow(18), 0, RoundingMode.DOWN);
                            // 若更低價則更新價格，寫入檔案
                            BigDecimal nowValue;
                            if (results.get(name) != null) {
                                nowValue = results.get(name);
                                if (price.compareTo(nowValue) < 0) {
                                    results.put(name, price);
                                    updateSingleEntry(name, price);
                                }
                            } else {
                                results.put(name, price);
                                updateSingleEntry(name, price);
                            }

                            // 加入完成搜索列表
                            foundValues.addAll(values.stream().filter(name::contains).toList());
                        }
                    }
                }

                // 若所有 values 都已被找到，跳出迴圈
                if (foundValues.containsAll(values)) break;

                // 檢查是否為最後一頁，若有下一頁繼續迴圈
                hasMorePage = !(Boolean) pagination.get("isLastPage");
                pageNo++;
            }
        }
    }

    private Map<String, Object> createRequestBody(String keyword, int pageNo) {
        Setting.GLOBAL_LOGGER.trace("[createRequestBody]");
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> filter = new HashMap<>();
        filter.put("name", keyword);
        filter.put("categoryNo", 0);
        filter.put("price", Map.of("min", Setting.ZERO, "max", 500000));
        filter.put("level", Map.of("min", Setting.ZERO, "max", 250));
        filter.put("starforce", Map.of("min", Setting.ZERO, "max", 25));
        filter.put("potential", Map.of("min", Setting.ZERO, "max", 4));
        filter.put("bonusPotential", Map.of("min", Setting.ZERO, "max", 4));

        body.put("filter", filter);
        body.put("sorting", "ExploreSorting_LOWEST_PRICE");
        body.put("paginationParam", Map.of("pageNo", pageNo, "pageSize", Setting.PAGE_SIZE));
        return body;
    }

    private HttpHeaders createHeaders() {
        Setting.GLOBAL_LOGGER.trace("[createHeaders]");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", "*/*");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");
        headers.add("Connection", "keep-alive");
        headers.add("Cookie", "utwat");
        headers.add("Cookie", "urwrt");
        headers.add("Host", "msu.io");
        return headers;
    }

    public Map<String, Object> loadLatestResult() throws IOException {
        Setting.GLOBAL_LOGGER.trace("[loadLatestResult]");
        return mapper.readValue(Setting.OUTPUT_FILE, new TypeReference<Map<String, Object>>() {
        });
    }

    // 更新單筆並覆寫到 output.json
    private void updateSingleEntry(String name, BigDecimal price) throws IOException {
        Map<String, BigDecimal> fileData = new HashMap<>();

        // 讀取舊資料
        if (Setting.OUTPUT_FILE.exists()) {
            try {
                fileData = mapper.readValue(Setting.OUTPUT_FILE, new TypeReference<Map<String, BigDecimal>>() {});
            } catch (IOException e) {
                Setting.GLOBAL_LOGGER.warn("Error reading output file during single update: {}", e.getMessage());
            }
        }

        // 比對價格有變才寫入
        if (!price.equals(fileData.get(name))) {
            fileData.put(name, price); // 更新單筆
            mapper.writerWithDefaultPrettyPrinter().writeValue(Setting.OUTPUT_FILE, fileData); // 覆寫整份（保留其他筆）
        }
    }
}

