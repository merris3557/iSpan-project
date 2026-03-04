package com.example.demo.Feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Feedback.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

}
