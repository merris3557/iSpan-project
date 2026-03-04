package com.example.demo.MapSearch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 地址轉經緯度服務
 * 使用 OpenStreetMap Nominatim 免費 API（無需 API Key）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?q={address}&format=json&limit=1&accept-language=zh-TW";

    /**
     * 將地址字串轉換為 [latitude, longitude]
     * 若轉換失敗，回傳 null（不擋儲存流程）
     */
    public BigDecimal[] geocode(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            // Nominatim 要求設定 User-Agent，否則可能被封鎖
            headers.set("User-Agent", "iSpanProject/1.0 (contact@example.com)");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 使用 ParameterizedTypeReference 解決 raw type 問題
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    NOMINATIM_URL,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    },
                    address);

            List<Map<String, Object>> results = response.getBody();
            if (results != null && !results.isEmpty()) {
                Map<String, Object> first = results.get(0);
                BigDecimal lat = new BigDecimal(first.get("lat").toString());
                BigDecimal lon = new BigDecimal(first.get("lon").toString());
                log.info("Geocoded '{}' → lat={}, lon={}", address, lat, lon);
                return new BigDecimal[] { lat, lon };
            } else {
                log.warn("Nominatim 查無結果：{}", address);
            }
        } catch (Exception e) {
            log.error("Geocoding 失敗（地址={}）：{}", address, e.getMessage());
        }
        return null;
    }
}
