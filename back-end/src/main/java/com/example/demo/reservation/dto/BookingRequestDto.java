package com.example.demo.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookingRequestDto {
    private Long userId; // 預約人的 ID
    private Integer storeId; // 店家的 ID
    private Integer reservedSeatType;
    private Integer guestCount;
    // 預約人姓名，必填且長度限制在 2 到 20 字之間
    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 20, message = "姓名長度需在 2 到 20 字之間")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z\\s.·]+$", message = "姓名僅能包含中英文、空格或點號")
    private String guestName;
    // 電話使用正規表達式(手機格式)
    @Pattern(regexp = "^09\\d{8}$", message = "手機格式不正確，需為 09 開頭的 10 位數字")
    private String guestPhone;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
