package com.example.demo.Feedback.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Feedback.dto.FeedbackRequestDto;
import com.example.demo.Feedback.entity.Feedback;
import com.example.demo.Feedback.entity.FeedbackTypes;
import com.example.demo.Feedback.repository.FeedbackRepository;
import com.example.demo.Feedback.repository.FeedbackStatusRepository;
import com.example.demo.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final FeedbackStatusRepository feedbackStatusRepo;
    private final UserRepository userRepo;

    public void createFeedback(FeedbackRequestDto dto) {

        // 檢查是否空值
        if (Strings.isBlank(dto.getEmail())) {
            throw new RuntimeException("Email is required");
        }

        // dto轉換entity
        Feedback feedback = new Feedback();
        feedback.setEmail(dto.getEmail());
        feedback.setName(dto.getName());
        feedback.setPhone(dto.getPhone());
        feedback.setContents(dto.getContents());

        // 處理關聯 (關鍵修正)
        // 處理 Type (從 DTO 拿到的 typeId)
        if (dto.getTypeId() != null) {
            FeedbackTypes type = new FeedbackTypes();
            type.setTypeId(dto.getTypeId());
            feedback.setFeedbackTypes(type);
        }

        // 寫入資料庫
        feedbackRepo.save(feedback);

    }
}