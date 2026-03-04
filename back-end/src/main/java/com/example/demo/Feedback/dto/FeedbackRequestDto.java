package com.example.demo.Feedback.dto;

import lombok.Data;

@Data
public class FeedbackRequestDto {
    private String name;

    private String phone;

    private String email;

    private String contents;

    private Long typeId;
}
