package com.example.demo.Feedback.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedback_status")
public class FeedbackStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long statusId;

    @Column(name = "name", nullable = false)
    private String statusName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feedbackStatus")
    private List<Feedback> feedbacks;
}
