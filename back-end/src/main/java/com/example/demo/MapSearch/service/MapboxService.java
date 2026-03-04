package com.example.demo.MapSearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 地址轉經緯度服務
 * 透過 MapboxService（Mapbox Geocoding API）取代原本的 Nominatim
 */
@Slf4j
@Service
public class MapboxService {
    // MapBox Key
    @Value("${mapBox.access-token}")
    private String accessToken;

    /**
     * 將地址字串轉換為 [latitude, longitude]（BigDecimal）
     * 若轉換失敗，回傳 null
     */
    public BigDecimal[] getCoordinate(String address) {
        if (address == null || address.isBlank())
            return null;

        // 加入 country=tw 與 language=zh-Hant 確保台灣地址精準度
        String encodedAddress = address.replace(" ", "%20");
        String url = String.format(
                "https://api.mapbox.com/geocoding/v5/mapbox.places/%s.json?access_token=%s&limit=1&country=tw&language=zh-Hant",
                encodedAddress, accessToken);

        try {
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");
                if (features != null && !features.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<Double> center = (List<Double>) features.get(0).get("center");
                    // center[0] 是經度 (lng), center[1] 是緯度 (lat)
                    BigDecimal lat = BigDecimal.valueOf(center.get(1));
                    BigDecimal lng = BigDecimal.valueOf(center.get(0));
                    log.info("Mapbox Geocoded '{}' → lat={}, lng={}", address, lat, lng);
                    return new BigDecimal[] { lat, lng };
                }
            }
            log.warn("Mapbox 查無結果：{}", address);
        } catch (Exception e) {
            log.error("Mapbox Geocoding 失敗（地址={}）：{}", address, e.getMessage());
        }
        return null;
    }
}
