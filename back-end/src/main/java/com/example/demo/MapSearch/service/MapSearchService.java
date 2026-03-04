package com.example.demo.MapSearch.service;

import com.example.demo.MapSearch.dto.StoreSearchResultDto;
import com.example.demo.MapSearch.repository.MapSearchRepository;
import com.example.demo.store.entity.OpenHour;
import com.example.demo.store.entity.StoresInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapSearchService {

    private final MapSearchRepository mapSearchRepository;
    private final GeocodingService geocodingService;

    // ─── Geocoding ───────────────────────────────────────────────────────────

    /**
     * 將地址 geocode 並填入 storeInfo，呼叫方需自行儲存
     */
    public void fillGeocode(StoresInfo storeInfo) {
        if (storeInfo.getAddress() == null || storeInfo.getAddress().isBlank())
            return;

        BigDecimal[] coords = geocodingService.geocode(storeInfo.getAddress());
        if (coords != null) {
            storeInfo.setLatitude(coords[0]);
            storeInfo.setLongitude(coords[1]);
        }
    }

    // ─── Search ──────────────────────────────────────────────────────────────

    /**
     * 搜尋店家。keyword 和 categories 均可為空。
     * - 兩者皆空 → 回傳全部
     * - 僅 keyword → 依關鍵字搜尋
     * - 僅 categories → 依標籤搜尋
     * - 兩者皆有 → 同時符合
     */
    public List<StoreSearchResultDto> search(String keyword, List<String> categories) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categories != null && !categories.isEmpty();

        List<StoresInfo> raw;

        if (hasKeyword && hasCategory) {
            raw = mapSearchRepository.findByKeywordAndCategoryNames(keyword, categories);
        } else if (hasKeyword) {
            raw = mapSearchRepository.findByKeyword(keyword);
        } else if (hasCategory) {
            raw = mapSearchRepository.findByCategoryNames(categories);
        } else {
            raw = mapSearchRepository.findAll();
        }

        // 只回傳有經緯度的店家（才能顯示在地圖上）
        return raw.stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 依 ID 查單一店家詳情（供店家介紹頁 API 使用）
     */
    public Optional<StoreSearchResultDto> findById(Integer storeId) {
        return mapSearchRepository.findById(storeId).map(this::toDto);
    }

    // ─── 私有輔助 ─────────────────────────────────────────────────────────────

    private StoreSearchResultDto toDto(StoresInfo s) {
        List<String> categoryNames = (s.getCategories() != null)
                ? s.getCategories().stream()
                        .map(c -> c.getCategoryName())
                        .collect(Collectors.toList())
                : new ArrayList<>();

        String openHoursSummary = buildOpenHoursSummary(s.getOpenHour());

        return StoreSearchResultDto.builder()
                .storeId(s.getStoreId())
                .storeName(s.getStoreName())
                .coverImage(s.getCoverImage())
                .description(s.getDescription())
                .address(s.getAddress())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .categories(categoryNames)
                .openHoursSummary(openHoursSummary)
                .build();
    }

    /**
     * 將 OpenHour list 轉成簡短摘要字串
     */
    private String buildOpenHoursSummary(List<OpenHour> openHours) {
        if (openHours == null || openHours.isEmpty())
            return "營業時間未提供";
        // 取第一筆作為代表
        OpenHour first = openHours.get(0);
        return first.getOpenTime() + " - " + first.getCloseTime();
    }
}
