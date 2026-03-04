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
@Table(name = "feedback_types")
public class FeedbackTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long typeId;

    @Column(name = "name", nullable = false)
    private String typeName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feedbackTypes")
    private List<Feedback> feedbacks;

}
