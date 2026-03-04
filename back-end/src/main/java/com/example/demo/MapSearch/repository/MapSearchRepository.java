package com.example.demo.MapSearch.repository;

import com.example.demo.store.entity.StoresInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MapSearchRepository extends JpaRepository<StoresInfo, Integer> {

        /**
         * 依關鍵字搜尋（店名 OR 描述）
         * 對應資料表：stores_info
         */
        @Query("SELECT DISTINCT s FROM StoresInfo s " +
                        "WHERE LOWER(s.storeName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "   OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<StoresInfo> findByKeyword(@Param("keyword") String keyword);

        /**
         * 依標籤 ID 列表搜尋（店家須符合所有選取標籤）
         * GROUP BY + HAVING COUNT 確保 AND 語意（非 OR）
         */
        @Query("SELECT s FROM StoresInfo s JOIN s.categories c " +
                        "WHERE c.categoryId IN :categoryIds " +
                        "GROUP BY s " +
                        "HAVING COUNT(DISTINCT c.categoryId) = :categoryCount")
        List<StoresInfo> findByCategoryIds(
                        @Param("categoryIds") List<Integer> categoryIds,
                        @Param("categoryCount") long categoryCount);

        /**
         * 依關鍵字 AND 所有標籤搜尋（同時符合兩個條件）
         * 店家須同時符合關鍵字以及所有選取標籤
         */
        @Query("SELECT s FROM StoresInfo s JOIN s.categories c " +
                        "WHERE c.categoryId IN :categoryIds " +
                        "  AND (LOWER(s.storeName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "    OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "GROUP BY s " +
                        "HAVING COUNT(DISTINCT c.categoryId) = :categoryCount")
        List<StoresInfo> findByKeywordAndCategoryIds(
                        @Param("keyword") String keyword,
                        @Param("categoryIds") List<Integer> categoryIds,
                        @Param("categoryCount") long categoryCount);
}
