package com.example.demo.Feedback.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Feedback.dto.FeedbackResponseDto;
import com.example.demo.Feedback.dto.ReplyRequestDto;
import com.example.demo.Feedback.entity.FeedbackStatus;
import com.example.demo.Feedback.service.FeedbackAPService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/feedbackList")
@RequiredArgsConstructor
@CrossOrigin
public class FeedbackAPController {

    private final FeedbackAPService feedbackAPService;

    @GetMapping
    public Page<FeedbackResponseDto> getFeedbacks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page) {

        return feedbackAPService.getFeedbacksPaged(status, page);
    }

    @GetMapping("/status-list")
    public List<FeedbackStatus> getStatusList() {
        return feedbackAPService.getAllStatuses();
    }

    @PutMapping("/reply")
    public void replyFeedback(@RequestBody ReplyRequestDto dto) {
        feedbackAPService.replyFeedback(dto);
    }

}
