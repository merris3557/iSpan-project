package com.example.demo.store.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

// 用於更新店家資訊的 DTO
@Data
public class StoreCreateUpdateDto {
    private String storeName;

    private String description;

    private String address;

    // 電話使用正規表達式(市話或手機格式)
    // @Pattern(regexp = "^$|^(09\\d{8}|0\\d{1,2}-?\\d{6,8})$", message = "電話格式不正確，請輸入正確的手機或市話格式")
    private String storePhone;

    private Integer timeSlot;

    private Integer timeLimit;

    // 接收上傳的圖片檔案
    private MultipartFile imageFile;

    // 是否刪除目前圖片
    private Boolean removeImage;

    // 建立店家標籤
    private List<Integer> categoryIds;

    // 指示是否更新標籤 (用於標籤清空邏輯)
    private Boolean updateCategories;
}
