package com.example.demo.Feedback.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Feedback.dto.FeedbackListDto;
import com.example.demo.Feedback.dto.FeedbackRequestDto;
import com.example.demo.Feedback.entity.Feedback;
import com.example.demo.Feedback.entity.FeedbackStatus;
import com.example.demo.Feedback.entity.FeedbackTypes;
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
            mailService.sendHtmlConfirmation(dto.getEmail(), "F" + System.currentTimeMillis(), dto.getUserId() != null);
        } catch (Exception e) {
            System.err.println("郵件寄送失敗，但資料已存入資料庫: " + e.getMessage());
        }

    }

    @GetMapping("/typeList")
    public List<FeedbackTypes> getTypeList() {
        return feedbackService.getAllTypes();
    }

    @GetMapping("/userInfoList")
    public ResponseEntity<List<FeedbackListDto>> getFeedbackListByUserId(
            @AuthenticationPrincipal UserDetails userDetails) {
        // userDetails.getUsername() 在你的 JwtTokenProvider 實作中就是 email
        String email = userDetails.getUsername();
        List<FeedbackListDto> list = feedbackService.getFeedbacksByEmail(email);
        return ResponseEntity.ok(list);
    }

}
