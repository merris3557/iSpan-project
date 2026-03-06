package com.example.demo.store.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.user.UserRepository;
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

@Service
public class StoreOperationService extends StoreBaseService {

    private final OpenHourRepository openHourRepository;
    private final SeatRepository seatRepository;
    private final OffDayRepository offDayRepository;

    // 手動寫建構子來調用 super(...)
    public StoreOperationService(UserRepository userRepository,
            StoreInfoRepository storeInfoRepository,
            OpenHourRepository openHourRepository,
            SeatRepository seatRepository,
            OffDayRepository offDayRepository) {
        super(userRepository, storeInfoRepository);
        this.openHourRepository = openHourRepository;
        this.seatRepository = seatRepository;
        this.offDayRepository = offDayRepository;
    }

    // 取得目前店家的預約設定
    public ReservationSettingsDto getReservationSettings() {
        // 1. 取得目前登入的店家資訊
        StoresInfo store = getMyStore();
        Integer storeId = store.getStoreId();

        ReservationSettingsDto dto = new ReservationSettingsDto();
        dto.setTimeSlot(store.getTimeSlot());
        dto.setTimeLimit(store.getTimeLimit());

        // 2. 獲取營業時間
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

        // 3. 獲取座位設定
        List<ReservationSettingsDto.SeatSettingsDto> sDtos = seatRepository.findById_StoreId(storeId).stream()
                .map(s -> {
                    ReservationSettingsDto.SeatSettingsDto sDto = new ReservationSettingsDto.SeatSettingsDto();
                    sDto.setSeatType(s.getId().getSeatType());
                    sDto.setTotalCount(s.getTotalCount());
                    return sDto;
                }).toList();
        dto.setSeatSettings(sDtos);

        // 4. 獲取休假設定
        List<ReservationSettingsDto.OffDayDto> odDtos = offDayRepository.findByStore_StoreId(storeId).stream()
                .map(od -> {
                    ReservationSettingsDto.OffDayDto odDto = new ReservationSettingsDto.OffDayDto();
                    odDto.setOffDate(od.getOffDate() != null ? od.getOffDate().toString() : null);
                    odDto.setDayOfWeek(od.getDayOfWeek());
                    odDto.setStartTime(od.getStartTime() != null ? od.getStartTime().toString() : null);
                    odDto.setEndTime(od.getEndTime() != null ? od.getEndTime().toString() : null);
                    return odDto;
                }).toList();
        dto.setOffDays(odDtos);

        return dto;
    }

    // 更新店家座位、營業時間與時段設定
    @Transactional
    public void updateReservationSettings(ReservationSettingsDto dto) {
        // 1. 取得目前登入的店家資訊
        StoresInfo store = getMyStore();

        Integer storeId = store.getStoreId();

        // 2. 更新店家的基礎設定 (時段、限時)
        store.setTimeSlot(dto.getTimeSlot());
        store.setTimeLimit(dto.getTimeLimit());
        storeInfoRepository.save(store);

        // 3. 更新營業時間 (先刪除，後新增)
        openHourRepository.deleteByStore_StoreId(storeId);
        openHourRepository.flush(); // 強制執行刪除

        if (dto.getOpenHours() != null) {
            List<OpenHour> newOpenHours = dto.getOpenHours().stream().map(hDto -> {
                LocalTime open = LocalTime.parse(hDto.getOpenTime());
                LocalTime close = LocalTime.parse(hDto.getCloseTime());

                // 阻擋跨日營業時間
                if (!close.isAfter(open)) {
                    throw new IllegalArgumentException("營業時間設定錯誤：結束時間必須晚於開始時間（星期 " + hDto.getDayOfWeek() + "）");
                }

                OpenHour oh = new OpenHour();
                oh.setStore(store);
                oh.setDayOfWeek(hDto.getDayOfWeek());
                oh.setOpenTime(open);
                oh.setCloseTime(close);
                return oh;
            }).toList();
            openHourRepository.saveAll(newOpenHours);
        }

        // 4. 更新座位設定 (先刪除，後新增)
        seatRepository.deleteById_StoreId(storeId);
        seatRepository.flush(); // 強制執行刪除

        if (dto.getSeatSettings() != null) {
            List<Seat> newSeats = dto.getSeatSettings().stream().map(sDto -> {
                Seat seat = new Seat();
                // 處理複合主鍵 SeatId
                SeatId sid = new SeatId(storeId, sDto.getSeatType());
                seat.setId(sid);
                seat.setStore(store);
                seat.setTotalCount(sDto.getTotalCount());
                return seat;
            }).toList();
            seatRepository.saveAll(newSeats);
        }

        // 5. 更新休假設定 (先刪除，後新增)
        offDayRepository.deleteByStore_StoreId(storeId);
        offDayRepository.flush(); // 強制執行刪除

        if (dto.getOffDays() != null) {
            List<OffDay> newOffDays = dto.getOffDays().stream().map(odDto -> {
                OffDay od = new OffDay();
                od.setStore(store);

                // 處理特定店休(特定日期)
                if (odDto.getOffDate() != null && !odDto.getOffDate().isEmpty()) {
                    od.setOffDate(LocalDate.parse(odDto.getOffDate()));
                }

                // 處理固定店休(星期幾)
                od.setDayOfWeek(odDto.getDayOfWeek());

                // 處理特定時段店休(例如：12:00-13:00)
                if (odDto.getStartTime() != null && !odDto.getStartTime().isEmpty()) {
                    od.setStartTime(LocalTime.parse(odDto.getStartTime()));
                }
                if (odDto.getEndTime() != null && !odDto.getEndTime().isEmpty()) {
                    od.setEndTime(LocalTime.parse(odDto.getEndTime()));
                }

                // 阻擋跨日或無效的店休時間
                if (od.getStartTime() != null && od.getEndTime() != null) {
                    if (!od.getEndTime().isAfter(od.getStartTime())) {
                        throw new IllegalArgumentException("店休時間設定錯誤：結束時間必須晚於開始時間");
                    }
                }

                return od;
            }).toList();
            offDayRepository.saveAll(newOffDays);
        }

    }
}