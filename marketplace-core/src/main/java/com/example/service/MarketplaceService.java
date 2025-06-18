package com.example.service;

import com.example.enums.Setting;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
        Map<String, List<String>> keywordMap = mapper.readValue(input, new TypeReference<>() {
        });
        // 取出 "others" 的值並轉換為 List<String>
        List<String> others = keywordMap.remove("others"); // 先移除原本的 "others" 鍵，避免干擾

        if (others != null) {
            for (String item : others) {
                // 為每個 item 加入一筆新的 key-value
                keywordMap.put(item, Collections.singletonList(item));
            }
        }

        Map<String, Map<String, Object>> results = new HashMap<>();
        // 若已有 output.json，先載入舊資料
        if (Setting.OUTPUT_FILE.exists()) {
            try {
                List<Map<String, Object>> oldList = mapper.readValue(Setting.OUTPUT_FILE,
                    new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> entry : oldList) {
                    String name = (String) entry.get("name");
                    BigDecimal price = new BigDecimal(entry.get("price").toString());
                    Long updateTimeUTC = Long.parseLong(entry.get("updateTimeUTC").toString());
                    Long categoryNo = Long.parseLong(entry.get("categoryNo").toString());

                    Map<String, Object> info = new HashMap<>();
                    info.put("price", price);
                    info.put("updateTimeUTC", updateTimeUTC);
                    info.put("categoryNo", categoryNo);

                    results.put(name, info);
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

                Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] start fetching item: {}", keyword);
                ResponseEntity<Map> response = safeExchangeWithRetry(request);

                // 每次請求後隨機停止 4 ~ 4.2 秒
                int jitter = new Random().nextInt(200);
                Thread.sleep(Setting.FETCH_INTERVAL_MILLISECOND + jitter);

                // response解析
                Map<String, Object> responseMap = (Map<String, Object>) response.getBody();
                if (responseMap == null) {
                    Setting.GLOBAL_LOGGER
                            .error("[fetchAndSaveLowestPrices] fetch complete but response not properly: {}", response);
                    break;
                }
                List<Map<String, Object>> items = (List<Map<String, Object>>) responseMap.get("items");
                Map<String, Object> pagination = (Map<String, Object>) responseMap.get("paginationResult");
                // 判斷總物品數量
                if ((int) pagination.get("totalCount") == 0) {
                    Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] fetch complete but no item found: {}",
                            response);
                    break;
                }

                Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] fetch complete and item found");

                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String name = (String) item.get("name");
                        Map<String, Object> category = (Map<String, Object>) item.get("category");
                        Long categoryNo = Long.parseLong(category.get("categoryNo").toString());
                        if (values.stream().anyMatch(name::contains)) {
                            Map<String, Object> salesInfo = (Map<String, Object>) item.get("salesInfo");
                            String priceWei = (String) salesInfo.get("priceWei");
                            if (priceWei == null || priceWei.isEmpty())
                                continue;
                            BigDecimal price = new BigDecimal(priceWei).divide(BigDecimal.TEN.pow(18), 0,
                                    RoundingMode.DOWN);
                            // 若更低價則更新價格，寫入檔案
                            if (results.get(name) != null) {
                                Map<String, Object> nowValue = results.get(name);
                                if (price.compareTo((BigDecimal) nowValue.get("price")) < 0) {
                                    results.put(name, createEntry(price, categoryNo));
                                    updateSingleEntry(name, price, categoryNo);
                                }
                            } else {
                                results.put(name, createEntry(price, categoryNo));
                                updateSingleEntry(name, price, categoryNo);
                            }

                            // 加入完成搜索列表
                            foundValues.addAll(values.stream().filter(name::contains).toList());
                        }
                    }
                }

                // 若所有 values 都已被找到，跳出迴圈
                if (foundValues.containsAll(values))
                    break;

                // 檢查是否為最後一頁，若有下一頁繼續迴圈
                hasMorePage = !(Boolean) pagination.get("isLastPage");
                pageNo++;
            }

            // 處理這次搜尋中未找到的 values
            for (String value : values) {
                if (!foundValues.contains(value)) {
                    Map<String, Object> valueItem;
                    // 記錄未找到的 value 為 -1
                    BigDecimal invalidPrice = BigDecimal.valueOf(-1);
                    try {
                        valueItem = results.get(value);
                        Long valueItemCategory = Long.parseLong(valueItem.get("CategoryNo").toString());
                        updateSingleEntry(value, invalidPrice, valueItemCategory);
                    } catch (Exception e) {
                        Setting.GLOBAL_LOGGER.warn("[fetchAndSaveLowestPrices] Item not found in exist file: {}", value);
                        updateSingleEntry(value, invalidPrice, -1L);
                    } finally {
                        Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] Item not found: {}", value);
                    }
                }
            }
        }
        Setting.GLOBAL_LOGGER.trace("[fetchAndSaveLowestPrices] Finish fetching all items.");
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
        headers.add("User-Agent", "PostmanRuntime/7.44.0");
        headers.add("Connection", "keep-alive");
        headers.add("Cookie", "utwat");
        headers.add("Cookie", "urwrt");
        headers.add("Cookie", "__cf_bm=546zbVM47ssBy6PeOS7UXkAT45G4KB_UlzbnJCA._fQ-1750121071-1.0.1.1-OGby8s_8VHNMoUT7IdQnfVwQUalxUm6.vw2YXf7TGlrp3F9gCWsp0Un5lB38iysXMXvGgfywdmJsWbohIfgzPa94pbRNeumB6V73_C7Fx0c");
        return headers;
    }

    private ResponseEntity<Map> safeExchangeWithRetry(HttpEntity<String> request) {
        int retryCount = 0;
        int backoffSeconds = 5;
        // TODO playwright implementation
        while (retryCount < Setting.MAX_RETRY) {
            try {
                Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] attempt {} to fetch item", retryCount + 1);
                return restTemplate.postForEntity(Setting.API_URL, request, Map.class);
            } catch (HttpClientErrorException e) {
                int statusCode = e.getStatusCode().value();
                Setting.GLOBAL_LOGGER.info("[fetchAndSaveLowestPrices] response: {}", e.getResponseBodyAsString());
                // 處理 Cloudflare challenge 或限制
                if (statusCode == 403 || statusCode == 429 || statusCode == 500) {
                    Setting.GLOBAL_LOGGER.warn("[safeExchangeWithRetry] Received status {}. Retrying after {} seconds...", statusCode, backoffSeconds);

                    try {
                        Thread.sleep((long) backoffSeconds * Setting.FETCH_INTERVAL_MILLISECOND);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    retryCount++;
                    backoffSeconds *= 2; // exponential backoff
                } else {
                    throw e; // 其他錯誤直接拋出
                }
            } catch (Exception ex) {
                Setting.GLOBAL_LOGGER.error("[safeExchangeWithRetry] Unexpected error: {}", ex.getMessage(), ex);
                throw ex;
            }
        }

        Setting.GLOBAL_LOGGER.error("Max retries exceeded when calling Marketplace API.");
        return null;
    }

    public Map<String, Object> loadLatestResult() throws IOException {
        Setting.GLOBAL_LOGGER.trace("[loadLatestResult]");
        return mapper.readValue(Setting.OUTPUT_FILE, new TypeReference<Map<String, Object>>() {
        });
    }

    // 更新單筆並覆寫到 output.json
    private void updateSingleEntry(String name, BigDecimal price, Long categoryNo) throws IOException {
        Map<String, Map<String, Object>> fileData = new HashMap<>();
        File parentDir = Setting.OUTPUT_FILE.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // 確保資料夾存在
        }

        // 讀取舊資料
        if (Setting.OUTPUT_FILE.exists()) {
            try {
                fileData = mapper.readValue(Setting.OUTPUT_FILE, new TypeReference<Map<String, Map<String, Object>>>(){});
            } catch (IOException e) {
                Setting.GLOBAL_LOGGER.warn("Error reading output file during single update: {}", e.getMessage());
            }
        } else {
            mapper.writerWithDefaultPrettyPrinter().writeValue(Setting.OUTPUT_FILE, "{}");
        }

        Map<String, Object> existing = fileData.get(name);
        BigDecimal oldPrice = existing != null && existing.get("price") != null
            ? new BigDecimal(existing.get("price").toString())
            : null;

        // 比對價格有變才寫入
        if (oldPrice == null || price.compareTo(oldPrice) != 0) {
            Map<String, Object> entry = createEntry(price, categoryNo);
            fileData.put(name, entry);

            // 排序
            LinkedHashMap<String, Map<String, Object>> sorted = fileData.entrySet().stream()
                .sorted(Comparator.comparing(e -> new BigDecimal(e.getValue().get("price").toString())))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));

            mapper.writerWithDefaultPrettyPrinter().writeValue(Setting.OUTPUT_FILE, sorted);
        }
    }

    private Map<String, Object> createEntry(BigDecimal price, Long categoryNo) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("price", price);
        entry.put("updateTimeUTC", Instant.now().getEpochSecond());
        entry.put("categoryNo", categoryNo);
        return entry;
    }
}
