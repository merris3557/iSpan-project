package com.example.demo.Feedback.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.demo.admin.Admin;
import com.example.demo.user.User;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String phone;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 500)
    private String contents;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String reply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private FeedbackStatus feedbackStatus;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private FeedbackTypes feedbackTypes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.feedbackStatus == null) {
            FeedbackStatus defaultStatus = new FeedbackStatus();
            defaultStatus.setStatusId(1L);
            this.feedbackStatus = defaultStatus;
        }
    }

}
