package com.example.demo.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class BookingUpdateDto {
    private String guestName;
    private String guestPhone;
    private LocalDate bookingDate;
    private LocalTime startTime;
}
