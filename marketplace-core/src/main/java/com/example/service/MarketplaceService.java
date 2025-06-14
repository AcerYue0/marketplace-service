package com.example.service;

import com.example.enums.Setting;
import com.example.util.MarketplaceItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class MarketplaceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public void fetchAndSaveLowestPrices() throws IOException {
        Map<String, List<String>> keywordMap = mapper.readValue(Setting.CLASSIFICATION_FILE,
                new TypeReference<Map<String, List<String>>>() {});

        Map<String, MarketplaceItem> results = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            String keyword = entry.getKey();
            List<String> values = entry.getValue();

            MarketplaceItem lowestItem = null;
            int pageNo = 1;
            boolean hasMorePage = true;
            Set<String> foundValues = new HashSet<>();

            while (hasMorePage) {
                Map<String, Object> body = createRequestBody(keyword, pageNo);
                HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), createHeaders());

                ResponseEntity<Map> response = restTemplate.exchange(Setting.API_URL, HttpMethod.POST, request, Map.class);

                // response解析
                Map<String, Object> responseMap = (Map<String, Object>) response.getBody();
                if (responseMap == null) break;
                List<Map<String, Object>> items = (List<Map<String, Object>>) responseMap.get("items");
                Map<String, Object> pagination = (Map<String, Object>) responseMap.get("paginationResult");

                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String name = (String) item.get("name");
                        if (values.stream().anyMatch(name::contains)) {
                            Map<String, Object> salesInfo = (Map<String, Object>) item.get("salesInfo");
                            String priceWei = (String) salesInfo.get("priceWei");
                            if (priceWei == null || priceWei.isEmpty()) continue;
                            BigDecimal price = new BigDecimal(priceWei).divide(BigDecimal.TEN.pow(18), 8, RoundingMode.DOWN);
                            if (lowestItem == null || price.compareTo(lowestItem.getPrice()) < 0) {
                                lowestItem = new MarketplaceItem(name, price, (String) item.get("imageUrl"));
                            }
                            foundValues.addAll(values.stream().filter(name::contains).toList());
                        }
                    }
                }

                // 若所有 values 都已被找到，跳出迴圈
                if (foundValues.containsAll(values)) break;

                hasMorePage = pagination != null && !(Boolean) pagination.get("isLastPage");
                pageNo++;
            }

            if (lowestItem != null) {
                results.put(keyword, lowestItem);
            }
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(Setting.OUTPUT_FILE, results);
    }

    private Map<String, Object> createRequestBody(String keyword, int pageNo) {
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "utwat");
        headers.add("Cookie", "urwrt");
        return headers;
    }
}

