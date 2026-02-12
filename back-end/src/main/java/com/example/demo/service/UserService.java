package com.example.demo.service;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserResponse;

import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getCurrentUser() {
        User user = getCurrentUserEntity();
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUserEntity();

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateStoreStatus(Long userId, Boolean isStore) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsStore(isStore);
        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
    }

    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * 修改密碼 (用戶必須提供當前密碼)
     */
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        User user = getCurrentUserEntity();

        // 驗證當前密碼
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // 更新為新密碼
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 取得當前用戶的 2FA 狀態
     */
    public boolean is2FAEnabled() {
        User user = getCurrentUserEntity();
        return Boolean.TRUE.equals(user.getTwoFactorEnabled());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .isStore(user.getIsStore())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
