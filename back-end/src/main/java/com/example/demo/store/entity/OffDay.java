package com.example.demo.store.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "off_days")
public class OffDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer offId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private StoresInfo store;

    @Column(name = "off_date")
    private LocalDate offDate;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;
}
