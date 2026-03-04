package com.example.demo.Feedback.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Feedback.dto.FeedbackRequestDto;
import com.example.demo.Feedback.service.FeedbackService;
import com.example.demo.Feedback.service.MailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@CrossOrigin
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final MailService mailService;

    @PostMapping
    public void createFeedback(@RequestBody FeedbackRequestDto dto) {

        feedbackService.createFeedback(dto);

        try {
            mailService.sendHtmlConfirmation(dto.getEmail(), "F" + System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("郵件寄送失敗，但資料已存入資料庫: " + e.getMessage());
        }

    }

}
