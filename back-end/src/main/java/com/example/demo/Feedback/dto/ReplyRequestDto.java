package com.example.demo.Feedback.dto;

import lombok.Data;

@Data
public class ReplyRequestDto {
    private Long feedbackId;

    private String reply;

    private Integer adminId;

    private Long statusId;

}
