package com.example.demo.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.reservation.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // 查詢特定店家、日期、座位類型下，與指定時間重疊的有效訂單數量
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.store.storeId = :storeId " +
            "AND b.bookingDate = :date AND b.reservedSeatType = :seatType " +
            "AND b.status = true " +
            "AND (CAST(:startTime AS time) < b.endTime AND CAST(:endTime AS time) > b.startTime)")
            // 這裡的時間重疊邏輯是：新訂單的開始時間 < 現有訂單的結束時間 AND 新訂單的結束時間 > 現有訂單的開始時間

    Integer countOverlappingBookings( // 跑完回傳一個int數字，代表有幾筆訂單跟目前的訂單時間重疊
            @Param("storeId") Integer storeId,
            @Param("date") LocalDate date,
            @Param("seatType") Integer seatType,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    // 查詢特定使用者的有效訂單，並按照預約日期和開始時間倒序排列
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = true " +
            "ORDER BY b.bookingDate DESC, b.startTime DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    // 查詢特定店家的有效訂單，並按照預約日期和開始時間倒序排列
    @Query("SELECT b FROM Booking b WHERE b.store.storeId = :storeId AND b.status = true " +
            "ORDER BY b.bookingDate DESC, b.startTime DESC")
    List<Booking> findByStoreId(@Param("storeId") Integer storeId);
}