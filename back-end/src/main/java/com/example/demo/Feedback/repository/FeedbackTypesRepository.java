package com.example.demo.Feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Feedback.entity.FeedbackTypes;

public interface FeedbackTypesRepository extends JpaRepository<FeedbackTypes, Long> {

}