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
     * GET /api/map/search?keyword=台式&categories=親子友善&categories=蔬食
     *
     * @param keyword    搜尋關鍵字（可選）
     * @param categories 標籤名稱列表（可選，支援多值）
     * @return 符合條件的店家列表（含經緯度，供地圖顯示）
     */
    @GetMapping("/search")
    public ResponseEntity<List<StoreSearchResultDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categories) {

        List<StoreSearchResultDto> results = mapSearchService.search(keyword, categories);
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
