package com.example.demo.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.store.entity.OffDay;

public interface OffDayRepository extends JpaRepository<OffDay, Long> {
    List<OffDay> findByStoreStoreId(Integer storeId);
}