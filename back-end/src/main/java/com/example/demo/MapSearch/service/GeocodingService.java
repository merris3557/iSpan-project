package com.example.demo.MapSearch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 地址轉經緯度服務
 * 委派給 MapboxService（Mapbox Geocoding API），已取代原本的 Nominatim
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final MapboxService mapboxService;

    /**
     * 將地址字串轉換為 [latitude, longitude]
     * 若轉換失敗，回傳 null（不擋儲存流程）
     */
    public BigDecimal[] geocode(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        BigDecimal[] coords = mapboxService.getCoordinate(address);
        if (coords == null) {
            log.warn("Mapbox 查無結果：{}", address);
        }
        return coords;
    }
}
