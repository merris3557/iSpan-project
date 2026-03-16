package com.example.demo.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Feedback.service.MailService;
import com.example.demo.reservation.dto.BookingRequestDto;
import com.example.demo.reservation.dto.BookingResponseDto;
import com.example.demo.reservation.dto.BookingUpdateDto;
import com.example.demo.reservation.dto.SlotAvailDto;
import com.example.demo.reservation.entity.Booking;
import com.example.demo.reservation.repository.BookingRepository;
import com.example.demo.store.dto.ReservationSettingsDto;
import com.example.demo.store.entity.OffDay;
import com.example.demo.store.entity.OpenHour;
import com.example.demo.store.entity.Seat;
import com.example.demo.store.entity.SeatId;
import com.example.demo.store.entity.StoresInfo;
import com.example.demo.store.repository.OffDayRepository;
import com.example.demo.store.repository.OpenHourRepository;
import com.example.demo.store.repository.SeatRepository;
import com.example.demo.store.repository.StoreInfoRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StoreInfoRepository storeInfoRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final OpenHourRepository openHourRepository;
    private final OffDayRepository offDayRepository;
    private final MailService mailService;

    // 取得店家訂位設定
    // 與StoreOperationService.java中的getStoreBookingConfig()方法幾乎相同，但不限定商家本人登入
    // 重用 ReservationSettingsDto
    public ReservationSettingsDto getStoreReservationConfig(Integer storeId) {
        // 1. 抓取商店基礎資訊
        StoresInfo store = storeInfoRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("找不到該店家"));
        // 2. 準備 DTO
        ReservationSettingsDto dto = new ReservationSettingsDto();
        dto.setName(store.getStoreName());
        dto.setTimeSlot(store.getTimeSlot());
        dto.setTimeLimit(store.getTimeLimit());
        // 3. 獲取營業時間
        List<ReservationSettingsDto.OpenHourDto> ohDtos = openHourRepository.findByStore_StoreId(storeId).stream()
                .map(oh -> {
                    ReservationSettingsDto.OpenHourDto ohDto = new ReservationSettingsDto.OpenHourDto();
                    ohDto.setDayOfWeek(oh.getDayOfWeek());
                    ohDto.setOpenTime(oh.getOpenTime().toString());
                    ohDto.setCloseTime(oh.getCloseTime().toString());
                    ohDto.setIsClosed(false);
                    return ohDto;
                }).toList();
        dto.setOpenHours(ohDtos);
        // 4. 獲取座位設定
        List<ReservationSettingsDto.SeatSettingsDto> sDtos = seatRepository.findById_StoreId(storeId).stream()
                .map(s -> {
                    ReservationSettingsDto.SeatSettingsDto sDto = new ReservationSettingsDto.SeatSettingsDto();
                    sDto.setSeatType(s.getId().getSeatType());
                    sDto.setTotalCount(s.getTotalCount());
                    return sDto;
                }).toList();
        dto.setSeatSettings(sDtos);
        return dto;
    }

    // 取得某一天某桌型的可訂位時段
    public List<SlotAvailDto> getAvailableSlots(Integer storeId, LocalDate date, Integer seatType) {
        // 獲取店家資訊
        StoresInfo store = storeInfoRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("找不到該店家"));

        // 獲取店家timeSlot (時段間隔) 和 timeLimit (用餐限時)
        Integer timeSlot = store.getTimeSlot();
        Integer timeLimit = store.getTimeLimit();

        // 取得當天是星期幾 (Java 的列舉 1-7)
        int dayOfWeek = date.getDayOfWeek().getValue();
        // 資料庫存 0(日)-6(六)，要做轉換
        int dbDayValue = (dayOfWeek == 7) ? 0 : dayOfWeek;

        // 獲取OpenHour
        OpenHour openHour = openHourRepository.findByStore_StoreIdAndDayOfWeek(storeId, dbDayValue)
                .orElseThrow(() -> new RuntimeException("該店家今日未營業"));

        // 使用複合主鍵 SeatId 查詢
        SeatId seatId = new SeatId(storeId, seatType);
        Seat seatConfig = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("該店未提供此桌型"));
        Integer totalTables = seatConfig.getTotalCount();

        // -----------------開始寫可訂位時段迴圈------------------
        List<SlotAvailDto> slots = new ArrayList<>();
        LocalTime currentTime = openHour.getOpenTime();
        LocalTime closeTime = openHour.getCloseTime(); // 最後可訂位時間

        while (!currentTime.isAfter(closeTime)) {
            // 1. 計算當前時段的末端
            LocalTime slotEnd = currentTime.plusMinutes(timeLimit);

            // 2. 檢查是否為店休
            if (isOffDay(storeId, date, currentTime, slotEnd)) {
                slots.add(new SlotAvailDto(currentTime.toString(), false));
                currentTime = currentTime.plusMinutes(timeSlot);
                continue;
            }

            // 3. 調用下方createBooking()方法裡面的 countOverlappingBookings() repository 方法
            long occupied = bookingRepository.countConflicts(
                    storeId, date, seatType, currentTime, slotEnd);

            // 4. 判斷是否為過去時間
            boolean isPast = date.equals(LocalDate.now()) && currentTime.isBefore(LocalTime.now());

            // 5. 判定並加入清單 (occupied < totalTables)
            boolean isAvailable = (occupied < totalTables) && !isPast;
            slots.add(new SlotAvailDto(currentTime.toString(), isAvailable));

            // 6. 往後跳過一個時段間隔
            currentTime = currentTime.plusMinutes(timeSlot);

        }

        return slots;
    }

    // 前端輸入建立訂位
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto dto) {
        // 1. 驗證使用者與店家是否存在
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("找不到該使用者"));
        StoresInfo store = storeInfoRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("找不到該店家"));

        // 2. 日期檢核：不得預約當天
        if (!dto.getBookingDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("僅開放預約明日以後的時段");
        }

        // 3. 處理結束時間 (如果 Dto 沒傳，則根據店家 timeLimit 計算)
        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = dto.getEndTime();
        if (endTime == null) {
            endTime = startTime.plusMinutes(store.getTimeLimit());
        }

        // 4. 檢查是否為營業日與店休
        int dayOfWeek = dto.getBookingDate().getDayOfWeek().getValue();
        int dbDayValue = (dayOfWeek == 7) ? 0 : dayOfWeek;

        OpenHour openHour = openHourRepository.findByStore_StoreIdAndDayOfWeek(dto.getStoreId(), dbDayValue)
                .orElseThrow(() -> new RuntimeException("該店家該日未營業"));

        if (startTime.isBefore(openHour.getOpenTime()) || startTime.isAfter(openHour.getCloseTime())) {
            throw new RuntimeException("所選時段非營業時間或已超過最後預約時間");
        }

        if (isOffDay(dto.getStoreId(), dto.getBookingDate(), startTime, endTime)) {
            throw new RuntimeException("所選時段適逢店休，無法預約");
        }

        // 5.悲觀鎖
        // 獲取該店該桌型的設定，並在此處「鎖定」該資源
        SeatId seatId = new SeatId(dto.getStoreId(), dto.getReservedSeatType());
        Seat seatConfig = seatRepository.findByIdForUpdate(seatId) // 這裡會鎖定該筆 Seat 記錄，直到本事務結束
                .orElseThrow(() -> new RuntimeException("該店家未設定此類別的座位"));

        // 在有鎖的情況下進行人數檢查與重疊查詢
        // 因為此處有鎖，其他執行緒如果也要訂「同店、同桌型」，會在此等待，直到本事務完成
        int totalTables = seatConfig.getTotalCount();

        long currentBookings = bookingRepository.countConflicts(
                dto.getStoreId(), dto.getBookingDate(), dto.getReservedSeatType(),
                startTime, endTime);

        // 如果已經滿了，直接丟出例外，讓使用者知道該時段已滿
        if (currentBookings >= totalTables) {
            throw new RuntimeException("該時段此類別座位已滿");
        }

        // 桌位人數限制：min = x-1 (最小 1), max = x
        int seatType = dto.getReservedSeatType();
        int minGuests = Math.max(1, seatType - 1);
        int maxGuests = seatType;
        if (dto.getGuestCount() < minGuests || dto.getGuestCount() > maxGuests) {
            throw new RuntimeException(String.format("%d人座之預約人數需介於 %d ~ %d 人之間",
                    seatType, minGuests, maxGuests));
        }

        // 6. 建立訂位實體並儲存
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setStore(store);
        booking.setReservedSeatType(dto.getReservedSeatType());
        booking.setGuestCount(dto.getGuestCount());
        booking.setGuestName(dto.getGuestName());
        booking.setGuestPhone(dto.getGuestPhone());
        booking.setBookingDate(dto.getBookingDate());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(true);

        // 7. 轉換回 Response Dto
        Booking savedBooking = bookingRepository.save(booking);

        // 8. 寄送郵件通知 (在回傳 response 之前呼叫)
        // 建議從 user 物件中拿 email，或者從 dto 拿
        mailService.sendBookingNotification(
                user.getEmail(), // 假設你的 User Entity 有 email 欄位
                savedBooking.getGuestName(),
                savedBooking.getStore().getStoreName(),
                savedBooking.getBookingDate().toString(),
                savedBooking.getStartTime().toString(),
                savedBooking.getGuestCount(),
                "NEW");

        return convertToResponse(savedBooking);
    }

    private BookingResponseDto convertToResponse(Booking booking) {
        BookingResponseDto resp = new BookingResponseDto();
        resp.setBookingId(booking.getBookingId());
        resp.setUserName(booking.getUser().getName()); // 使用 User.getName()
        resp.setStoreName(booking.getStore().getStoreName());
        resp.setReservedSeatType(booking.getReservedSeatType());
        resp.setGuestCount(booking.getGuestCount());
        resp.setGuestName(booking.getGuestName());
        resp.setGuestPhone(booking.getGuestPhone());
        resp.setBookingDate(booking.getBookingDate());
        resp.setStartTime(booking.getStartTime());
        resp.setEndTime(booking.getEndTime());
        resp.setStatus(booking.getStatus());
        resp.setCreatedAt(booking.getCreatedAt());
        return resp;
    }

    // ----------使用者訂位管理----------
    // 1. 獲取使用者的所有訂位列表
    public List<BookingResponseDto> getBookingsByUser(Long userId) {
        return bookingRepository.findByUser_Id(userId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    // 2. 更新訂位資訊（僅限姓名與電話）
    @Transactional
    public BookingResponseDto updateBooking(Integer bookingId, BookingUpdateDto dto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到該訂位紀錄"));

        booking.setGuestName(dto.getGuestName());
        booking.setGuestPhone(dto.getGuestPhone());

        Booking savedBooking = bookingRepository.save(booking);

        // 發送更新通知
        mailService.sendBookingNotification(
                savedBooking.getUser().getEmail(),
                savedBooking.getGuestName(),
                savedBooking.getStore().getStoreName(),
                savedBooking.getBookingDate().toString(),
                savedBooking.getStartTime().toString(),
                savedBooking.getGuestCount(),
                "UPDATE");

        return convertToResponse(savedBooking);
    }

    // 3. 取消訂位（軟刪除，將狀態改為 false）
    @Transactional
    public void cancelBooking(Integer bookingId) {
        // 1. 查找訂位紀錄
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到該訂位紀錄"));

        // 2. 執行軟刪除狀態變更
        booking.setStatus(false);
        
        // 3. 儲存並取得更新後的實體 (為了確保能拿到關聯的 User 和 Store 資訊)
        Booking savedBooking = bookingRepository.save(booking);

        // 4. 發送取消通知信
        // 傳入 "CANCEL" 觸發你在 MailService 中設定的紅色主題/文字
        mailService.sendBookingNotification(
                savedBooking.getUser().getEmail(),
                savedBooking.getGuestName(),
                savedBooking.getStore().getStoreName(),
                savedBooking.getBookingDate().toString(),
                savedBooking.getStartTime().toString(),
                savedBooking.getGuestCount(),
                "CANCEL");
    }

    // ----------店家訂位管理----------
    // 1. 取得特定店家的訂位紀錄
    public List<BookingResponseDto> getBookingsByStore(Integer storeId) {
        return bookingRepository.findByStore_StoreId(storeId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    // 2. 店家更新訂位資訊（可修改日期與時間，包含完備檢核）
    @Transactional
    public BookingResponseDto updateBookingByShop(Integer bookingId, BookingUpdateDto dto) {
        // 先獲取原本的訂位
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到該訂位紀錄"));

        // 準備新資料 (如果 Dto 沒傳則維持不變)
        LocalDate newDate = dto.getBookingDate() != null ? dto.getBookingDate() : booking.getBookingDate();
        LocalTime newStart = dto.getStartTime() != null ? dto.getStartTime() : booking.getStartTime();

        // 如果日期或時間有變動，必須進行營業時間與重疊檢核
        if (dto.getBookingDate() != null || dto.getStartTime() != null) {
            // 1. 日期檢核：不得修改為當天
            if (!newDate.isAfter(LocalDate.now())) {
                throw new RuntimeException("不得將預約修改為今日或過去的日期");
            }
            StoresInfo store = booking.getStore();

            // 2. 營業時間檢核
            int dayOfWeek = newDate.getDayOfWeek().getValue();
            int dbDayValue = (dayOfWeek == 7) ? 0 : dayOfWeek;
            OpenHour openHour = openHourRepository.findByStore_StoreIdAndDayOfWeek(store.getStoreId(), dbDayValue)
                    .orElseThrow(() -> new RuntimeException("該店家該日未營業"));

            if (newStart.isBefore(openHour.getOpenTime()) || newStart.isAfter(openHour.getCloseTime())) {
                throw new RuntimeException("所選時段非營業時間或已超過最後預約時間");
            }

            // 3. 容量與重疊檢核
            // 鎖定該座位類型資源 (悲觀鎖)
            SeatId seatId = new SeatId(store.getStoreId(), booking.getReservedSeatType());
            Seat seatConfig = seatRepository.findByIdForUpdate(seatId)
                    .orElseThrow(() -> new RuntimeException("找不到該桌型的座位設定"));

            int totalTables = seatConfig.getTotalCount();
            LocalTime newEnd = newStart.plusMinutes(store.getTimeLimit());

            // 4. 檢查是否為店休
            if (isOffDay(store.getStoreId(), newDate, newStart, newEnd)) {
                throw new RuntimeException("所選時段適逢店休");
            }

            // 5. 查詢重疊訂單 (排除自身，避免跟原本的自己衝突)
            long occupied = bookingRepository.countConflictsExcept(
                    store.getStoreId(), newDate, booking.getReservedSeatType(),
                    newStart, newEnd, bookingId);

            if (occupied >= totalTables) {
                throw new RuntimeException("該時段此類別座位已滿");
            }

            // 6. 通過檢核，執行更新
            booking.setBookingDate(newDate);
            booking.setStartTime(newStart);
            booking.setEndTime(newEnd);
        }

        // 基本資料更新 (guestName, guestPhone)
        if (dto.getGuestName() != null)
            booking.setGuestName(dto.getGuestName());
        if (dto.getGuestPhone() != null)
            booking.setGuestPhone(dto.getGuestPhone());

                Booking savedBooking = bookingRepository.save(booking);

        // 發送更新通知
        mailService.sendBookingNotification(
                savedBooking.getUser().getEmail(),
                savedBooking.getGuestName(),
                savedBooking.getStore().getStoreName(),
                savedBooking.getBookingDate().toString(),
                savedBooking.getStartTime().toString(),
                savedBooking.getGuestCount(),
                "UPDATE");

        return convertToResponse(savedBooking);
    }

    private boolean isOffDay(Integer storeId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 1. 取得店家所有的店休設定
        List<OffDay> offDays = offDayRepository.findByStore_StoreId(storeId);

        // 2. 計算這天是星期幾 (Java 是 1-7，我們資料庫存 0-6)
        int dayOfWeek = date.getDayOfWeek().getValue();
        int dbDayValue = (dayOfWeek == 7) ? 0 : dayOfWeek;

        // 3. 逐一比對店休規則
        for (OffDay off : offDays) {
            // 條件 A：特定日期剛好是今天
            boolean matchDate = (off.getOffDate() != null && off.getOffDate().equals(date));
            // 條件 B：常規店休剛好是星期幾
            boolean matchDayOfWeek = (off.getDayOfWeek() != null && off.getDayOfWeek().equals(dbDayValue));
            // 條件 C：每天常規休假 (日期與星期皆未設定)
            boolean matchDaily = (off.getOffDate() == null && off.getDayOfWeek() == null);

            // 如果都不是，看下一條規則
            if (!matchDate && !matchDayOfWeek && !matchDaily) {
                continue;
            }

            // 日期對上了，現在檢查「時間」是否有交集 (重疊)
            if (off.getStartTime() == null && off.getEndTime() == null) {
                return true; // 沒寫時間代表全天店休
            } else {
                // 重疊邏輯：預約開始時間 < 店休結束時間 且 預約結束時間 > 店休開始時間
                if (startTime.isBefore(off.getEndTime()) && endTime.isAfter(off.getStartTime())) {
                    return true;
                }
            }
        }
        return false;
    }
}
