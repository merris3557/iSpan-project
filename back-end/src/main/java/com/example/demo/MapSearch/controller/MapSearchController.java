package com.example.demo.MapSearch.controller;

import com.example.demo.MapSearch.dto.StoreSearchResultDto;
import com.example.demo.MapSearch.service.MapSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapSearchController {

    private final MapSearchService mapSearchService;

    /**
     * 搜尋店家 API
     * GET /api/map/search?keyword=台式&categoryIds=1&categoryIds=3
     *
     * @param keyword     搜尋關鍵字（可選），查詢 stores_info 資料表
     * @param categoryIds 標籤 ID 列表（可選，支援多值），透過 JOIN store_category_mapping 查詢
     * @return 符合條件的店家列表（含經緯度，供地圖顯示）
     */
    @GetMapping("/search")
    public ResponseEntity<List<StoreSearchResultDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Integer> categoryIds) {

        List<StoreSearchResultDto> results = mapSearchService.search(keyword, categoryIds);
        return ResponseEntity.ok(results);
    }

    /**
     * 店家詳情 API（供前端跳轉至店家介紹頁使用）
     * GET /api/map/store/{id}
     *
     * @param id 店家 ID
     * @return 店家詳細資訊，找不到則回傳 404
     */
    @GetMapping("/store/{id}")
    public ResponseEntity<StoreSearchResultDto> getStore(@PathVariable Integer id) {
        return mapSearchService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
