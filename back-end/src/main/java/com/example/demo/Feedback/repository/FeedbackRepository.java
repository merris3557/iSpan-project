package com.example.demo.Feedback.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Feedback.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByFeedbackStatus_StatusName(String statusName);

    Page<Feedback> findAll(Pageable pageable);

    Page<Feedback> findByFeedbackStatus_StatusName(String statusName, Pageable pageable);

}
