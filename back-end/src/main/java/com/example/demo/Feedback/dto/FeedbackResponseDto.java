package com.example.demo.Feedback.dto;

import lombok.Data;

@Data
public class FeedbackResponseDto {

    private Long id;

    private String name;

    private String phone;

    private String email;

    private String typeName;

    private String contents;

    private String createdAt;

    private String statusName;

    private String reply;

    private String repliedAt;

    private Integer adminId;
}
