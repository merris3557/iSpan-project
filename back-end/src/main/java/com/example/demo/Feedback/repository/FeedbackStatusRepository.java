package com.example.demo.Feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Feedback.entity.FeedbackStatus;

public interface FeedbackStatusRepository extends JpaRepository<FeedbackStatus, Long> {

    // for postgreSQL
    boolean existsByStatusName(String statusName);

}
