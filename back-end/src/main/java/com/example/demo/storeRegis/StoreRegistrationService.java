package com.example.demo.storeRegis;

import com.example.demo.admin.Admin;
import com.example.demo.admin.AdminRepository;
import com.example.demo.store.entity.StoresInfo;
import com.example.demo.store.repository.StoreInfoRepository;
import com.example.demo.storeRegis.dto.StoreRegistrationRequest;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreRegistrationService {

    private final StoreRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final StoreInfoRepository storeInfoRepository;
    private final AdminRepository adminRepository;

    // 1. 建立新申請
    @Transactional
    public StoreRegistration createApplication(Long userId, StoreRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 允許已是商家的使用者提交申請 (作為修改資料用)

        // 檢查是否有 PENDING 或 RETURNED 的申請
        // 若有 RETURNED，應引導使用者使用 updateApplication，但這邊先做嚴格阻擋
        boolean hasPendingOrReturned = registrationRepository.existsByApplicantAndStatusIn(
                user, List.of(StoreRegistrationStatus.PENDING, StoreRegistrationStatus.RETURNED));

        if (hasPendingOrReturned) {
            throw new RuntimeException("User already has a pending or returned application");
        }

        StoreRegistration registration = StoreRegistration.builder()
                .applicant(user)
                .email(user.getEmail())
                .name(request.getOwnerName() != null ? request.getOwnerName() : user.getName())
                .phone(request.getStorePhone())
                .address(request.getStoreAddress())
                .storeName(request.getStoreName()) // Optional
                .status(StoreRegistrationStatus.PENDING)
                .isUpdate(user.getIsStore()) // 標記是否為資料修改申請
                .build();

        return registrationRepository.save(registration);
    }

    // 2. 更新原單並重送 (針對 RETURNED 狀態)
    @Transactional
    public StoreRegistration updateApplication(Long userId, Integer registrationId, StoreRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StoreRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (!registration.getApplicant().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to this registration");
        }

        if (registration.getStatus() != StoreRegistrationStatus.RETURNED) {
            throw new RuntimeException("Only returned applications can be re-submitted");
        }

        // 更新資料
        if (request.getOwnerName() != null)
            registration.setName(request.getOwnerName());
        if (request.getStorePhone() != null)
            registration.setPhone(request.getStorePhone());
        if (request.getStoreAddress() != null)
            registration.setAddress(request.getStoreAddress());
        if (request.getStoreName() != null)
            registration.setStoreName(request.getStoreName());

        // 重置狀態與回覆
        registration.setStatus(StoreRegistrationStatus.PENDING);
        // registration.setReply(null); // 選擇性：保留舊的回覆直到下次審核，或者清空。這裡先保留。

        return registrationRepository.save(registration);
    }

    // 2.1 更新待審核申請 (針對 PENDING 狀態)
    @Transactional
    public StoreRegistration updatePendingApplication(Long userId, Integer registrationId,
            StoreRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StoreRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (!registration.getApplicant().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to this registration");
        }

        if (registration.getStatus() != StoreRegistrationStatus.PENDING) {
            throw new RuntimeException("Only PENDING applications can be modified here");
        }

        // 更新資料
        if (request.getOwnerName() != null)
            registration.setName(request.getOwnerName());
        if (request.getStorePhone() != null)
            registration.setPhone(request.getStorePhone());
        if (request.getStoreAddress() != null)
            registration.setAddress(request.getStoreAddress());
        if (request.getStoreName() != null)
            registration.setStoreName(request.getStoreName());

        return registrationRepository.save(registration);
    }

    // 3. 查詢所有申請 (依狀態篩選，可分頁)
    public Page<StoreRegistration> findAll(StoreRegistrationStatus status, Boolean isUpdate, Pageable pageable) {
        if (status != null && isUpdate != null) {
            return registrationRepository.findByStatusAndIsUpdate(status, isUpdate, pageable);
        } else if (status != null) {
            return registrationRepository.findByStatus(status, pageable);
        } else if (isUpdate != null) {
            return registrationRepository.findByIsUpdate(isUpdate, pageable);
        }
        return registrationRepository.findAll(pageable);
    }

    // 3.1 查詢特定使用者的申請
    public List<StoreRegistration> findByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return registrationRepository.findByApplicant(user);
    }

    // 4. 同意申請
    @Transactional
    public void approveApplication(Integer adminId, Integer registrationId, String opinion, String lastUpdatedAt) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        StoreRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (registration.getStatus() != StoreRegistrationStatus.PENDING) {
            throw new RuntimeException("Registration is not in PENDING status");
        }

        // Dirty Write Check
        if (lastUpdatedAt != null && !lastUpdatedAt.isEmpty()) {
            String currentUpdatedStr = registration.getUpdatedAt() != null ? registration.getUpdatedAt().toString()
                    : "";
            // Spring Boot 傳回前端的 LocalDateTime 可能帶有 Z 或毫秒差異，這裡做安全的字串比對，只比對到秒之後的前綴
            // 或是最簡單的直接 equals (前端保證原封不動傳回)
            if (!currentUpdatedStr.equals(lastUpdatedAt)) {
                // 如果直接 equals 失敗，嘗試移除格式差異 (例如 'Z' 或是毫秒後綴的長度差異)
                if (!currentUpdatedStr.startsWith(lastUpdatedAt.split("\\.")[0])) {
                    throw new RuntimeException("此申請資料已由申請人修改過，請重新整理頁面後再審核。");
                }
            }
        }

        // update registration
        registration.setStatus(StoreRegistrationStatus.APPROVED);
        registration.setManager(admin);
        registration.setReply(opinion);
        registrationRepository.save(registration);

        // update user
        User user = registration.getApplicant();
        user.setIsStore(true);
        userRepository.save(user);

        // create or update store info
        StoresInfo storeInfo = storeInfoRepository.findByUser(user).orElse(new StoresInfo());
        storeInfo.setUser(user);
        storeInfo.setStoreName(
                registration.getStoreName() != null ? registration.getStoreName() : registration.getName() + "的店"); // Fallback
                                                                                                                    // name
        storeInfo.setStorePhone(registration.getPhone());
        storeInfo.setAddress(registration.getAddress());

        // Default values only for new StoresInfo
        if (storeInfo.getStoreId() == null) {
            storeInfo.setTimeSlot(30);
            storeInfo.setTimeLimit(90);
        }

        storeInfoRepository.save(storeInfo);
    }

    // 5. 退回申請
    @Transactional
    public void rejectApplication(Integer adminId, Integer registrationId, String opinion, String lastUpdatedAt) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        StoreRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (registration.getStatus() != StoreRegistrationStatus.PENDING) {
            throw new RuntimeException("Registration is not in PENDING status");
        }

        // Dirty Write Check
        if (lastUpdatedAt != null && !lastUpdatedAt.isEmpty()) {
            String currentUpdatedStr = registration.getUpdatedAt() != null ? registration.getUpdatedAt().toString()
                    : "";
            if (!currentUpdatedStr.equals(lastUpdatedAt)) {
                if (!currentUpdatedStr.startsWith(lastUpdatedAt.split("\\.")[0])) {
                    throw new RuntimeException("此申請資料已由申請人修改過，請重新整理頁面後再審核。");
                }
            }
        }

        registration.setStatus(StoreRegistrationStatus.RETURNED);
        registration.setManager(admin);
        registration.setReply(opinion);

        registrationRepository.save(registration);
    }
}
