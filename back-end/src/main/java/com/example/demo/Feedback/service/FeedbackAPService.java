package com.example.demo.Feedback.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.Feedback.dto.FeedbackResponseDto;
import com.example.demo.Feedback.dto.ReplyRequestDto;
import com.example.demo.Feedback.entity.Feedback;
import com.example.demo.Feedback.entity.FeedbackStatus;
import com.example.demo.Feedback.repository.FeedbackRepository;
import com.example.demo.Feedback.repository.FeedbackStatusRepository;
import com.example.demo.admin.Admin;
import com.example.demo.admin.AdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackAPService {

    private final FeedbackRepository feedbackRepo;
    private final MailService mailService;
    private final FeedbackStatusRepository statusRepo;
    private final AdminRepository adminRepo;

    public Page<FeedbackResponseDto> getFeedbacksPaged(String statusName, int page) {
        // 設定每頁 10 筆，並按 createdAt 倒序排列 (最新的在前面)
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());

        Page<Feedback> entityPage;

        if (statusName != null && !statusName.isBlank()) {
            // 按狀態分頁查詢
            entityPage = feedbackRepo.findByFeedbackStatus_StatusName(statusName, pageable);
        } else {
            // 全部分頁查詢
            entityPage = feedbackRepo.findAll(pageable);
        }

        // 利用 Page 的 map 功能轉換為 DTO，這會自動保留所有分頁資訊
        return entityPage.map(this::convertToDto);
    }

    public List<FeedbackStatus> getAllStatuses() {
        return statusRepo.findAll();
    }

    private FeedbackResponseDto convertToDto(Feedback feedback) {
        FeedbackResponseDto dto = new FeedbackResponseDto();

        dto.setId(feedback.getId());
        dto.setName(feedback.getName());
        dto.setPhone(feedback.getPhone());
        dto.setEmail(feedback.getEmail());
        dto.setContents(feedback.getContents());
        dto.setReply(feedback.getReply());

        // 2. 處理客訴種類名稱 (Entity 為物件，DTO 為 String)
        if (feedback.getFeedbackTypes() != null) {
            // 這裡假設你的 FeedbackTypes Entity 裡面名稱欄位叫 typeName
            dto.setTypeName(feedback.getFeedbackTypes().getTypeName());
        }

        // 3. 處理處理狀態名稱 (Entity 為物件，DTO 為 String)
        if (feedback.getFeedbackStatus() != null) {
            // 這裡假設你的 FeedbackStatus Entity 裡面名稱欄位叫 statusName
            dto.setStatusName(feedback.getFeedbackStatus().getStatusName());
        }

        // 4. 處理時間轉字串 (LocalDateTime -> String)
        if (feedback.getCreatedAt() != null) {
            dto.setCreatedAt(feedback.getCreatedAt().toString());
        }
        if (feedback.getRepliedAt() != null) {
            dto.setRepliedAt(feedback.getRepliedAt().toString());
        }

        // 5. 處理管理員 ID (從 Admin 物件中取得)
        if (feedback.getAdmin() != null) {
            // 假設你的 Admin Entity 裡的 ID 叫 adminId
            dto.setAdminId(feedback.getAdmin().getId());
        }

        return dto;

    }

    public void replyFeedback(ReplyRequestDto dto) {
        // 1. 從 DTO 取得回覆內容和反饋 ID
        String replyContent = dto.getReply();
        Long feedbackId = dto.getFeedbackId();

        // 2. 根據反饋 ID 從資料庫中找到對應的 Feedback 實體
        Feedback feedback = feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found with ID: " + feedbackId));

        // 3. 更新 Feedback 實體的回覆內容和回覆時間
        feedback.setReply(replyContent);
        feedback.setRepliedAt(java.time.LocalDateTime.now());
        // 3. 處理 Admin 物件 (關鍵點！)
        if (dto.getAdminId() != null) {
            Admin admin = adminRepo.findById(dto.getAdminId())
                    .orElseThrow(() -> new RuntimeException("找不到管理員 ID: " + dto.getAdminId()));
            feedback.setAdmin(admin); // 這裡是傳入物件，不是傳 ID
        }

        // 4. 處理狀態物件
        if (dto.getStatusId() != null) {
            FeedbackStatus status = statusRepo.findById(dto.getStatusId())
                    .orElseThrow(() -> new RuntimeException("找不到狀態 ID: " + dto.getStatusId()));
            feedback.setFeedbackStatus(status);
        }

        // 5. 存檔
        feedbackRepo.save(feedback);

        mailService.replyNotification(
                feedback.getEmail(),
                feedback.getCaseNumber(), // 使用你 Entity 裡的 caseNumber
                dto.getReply(),
                feedback.getUser() != null);
    }

    public void deleteFeedback(Long id) {
        if (!feedbackRepo.existsById(id)) {
            throw new RuntimeException("Feedback not found with ID: " + id);
        }
        feedbackRepo.deleteById(id);
    }

}
