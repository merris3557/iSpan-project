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
         */
        @Query("SELECT DISTINCT s FROM StoresInfo s " +
                        "WHERE LOWER(s.storeName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "   OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<StoresInfo> findByKeyword(@Param("keyword") String keyword);

        /**
         * 依標籤名稱列表搜尋
         */
        @Query("SELECT DISTINCT s FROM StoresInfo s JOIN s.categories c " +
                        "WHERE c.categoryName IN :categoryNames")
        List<StoresInfo> findByCategoryNames(@Param("categoryNames") List<String> categoryNames);

        /**
         * 依關鍵字 AND 標籤搜尋（同時符合兩個條件）
         */
        @Query("SELECT DISTINCT s FROM StoresInfo s JOIN s.categories c " +
                        "WHERE c.categoryName IN :categoryNames " +
                        "  AND (LOWER(s.storeName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "    OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        List<StoresInfo> findByKeywordAndCategoryNames(
                        @Param("keyword") String keyword,
                        @Param("categoryNames") List<String> categoryNames);
}
