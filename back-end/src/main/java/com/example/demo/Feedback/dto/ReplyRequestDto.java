package com.example.demo.Feedback.dto;

import lombok.Data;

@Data
public class ReplyRequestDto {
    private Long id;

    private String reply;

    private Long statusId;

    private Long adminId;

}
