package com.example.demo.reservation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.reservation.dto.BookingRequestDto;
import com.example.demo.reservation.dto.BookingResponseDto;
import com.example.demo.reservation.dto.BookingUpdateDto;
import com.example.demo.reservation.dto.SlotAvailDto;
import com.example.demo.reservation.service.BookingService;
import com.example.demo.store.dto.ReservationSettingsDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // 取得店家訂位設定
    @GetMapping("/config/{storeId}")
    public ResponseEntity<ReservationSettingsDto> getStoreConfig(@PathVariable Integer storeId) {
        ReservationSettingsDto config = bookingService.getStoreReservationConfig(storeId);
        return ResponseEntity.ok(config);
    }

    // 取得某一天某桌型的可訂位時段
    @GetMapping("/available-slots")
    public ResponseEntity<List<SlotAvailDto>> getSlots(
            @RequestParam Integer storeId,
            @RequestParam LocalDate date,
            @RequestParam Integer seatType) {

        List<SlotAvailDto> response = bookingService.getAvailableSlots(storeId, date, seatType);
        return ResponseEntity.ok(response);
    }

    // 建立訂位
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody BookingRequestDto dto) {
        BookingResponseDto response = bookingService.createBooking(dto);
        return ResponseEntity.ok(response);
    }

    // ----------使用者訂位管理----------
    // 1. 取得使用者的訂位紀錄
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getBookingsByUser(@PathVariable Long userId) {
        List<BookingResponseDto> response = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. 更新訂位（目前僅限姓名與電話）
    @PutMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDto>> updateBooking(
            @PathVariable Integer bookingId,
            @RequestBody BookingUpdateDto dto) {
        BookingResponseDto response = bookingService.updateBooking(bookingId, dto);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    // 3. 取消訂位（軟刪除）
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Integer bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("訂位已取消", null));
    }

    // ----------店家訂位管理----------
    // 1. 取得特定店家的訂位紀錄
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getBookingsByStore(@PathVariable Integer storeId) {
        List<BookingResponseDto> response = bookingService.getBookingsByStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. 店家更新訂位資訊（可修改日期與時間）
    @PutMapping("/store/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDto>> updateBookingByShop(
            @PathVariable Integer bookingId,
            @RequestBody BookingUpdateDto dto) {
        BookingResponseDto response = bookingService.updateBookingByShop(bookingId, dto);
        return ResponseEntity.ok(ApiResponse.success("店家更新成功", response));
    }
}
