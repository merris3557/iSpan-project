package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserResponse;

import com.example.demo.entity.User;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider tokenProvider;
        private final AuthenticationManager authenticationManager;

        @Value("${jwt.access-token-expiration-ms}")
        private long accessTokenExpirationMs;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
                }

                // 如果沒有提供 name，使用 email 的 @ 前綴作為預設名稱
                String displayName = request.getName();
                if (displayName == null || displayName.isBlank()) {
                        displayName = request.getEmail().split("@")[0];
                }

                User user = User.builder()
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .name(displayName)
                                .isStore(false)
                                .enabled(true)
                                .build();

                user = userRepository.save(user);

                String accessToken = tokenProvider.generateAccessToken(user.getEmail(),
                                Boolean.TRUE.equals(user.getIsStore()) ? "STORE" : "USER");
                String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(accessTokenExpirationMs / 1000) // in seconds
                                .user(mapToUserResponse(user))
                                .build();
        }

        public AuthResponse login(LoginRequest request) {
                // 使用 identifier（可以是 email 或 username）進行登入
                String identifier = request.getIdentifier();

                // 第一步：驗證用戶名和密碼
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                identifier,
                                                request.getPassword()));

                User user = userRepository.findByEmail(identifier)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // 第二步：檢查是否啟用 2FA
                if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
                        // 如果啟用了 2FA，必須提供驗證碼
                        if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                                throw new RuntimeException("2FA code is required");
                        }

                        // 驗證 2FA 碼
                        if (!verify2FACode(user.getTwoFactorSecret(), request.getTwoFactorCode())) {
                                throw new RuntimeException("Invalid 2FA code");
                        }
                }

                // 第三步：生成 JWT tokens
                // login 使用 authentication，JwtTokenProvider 改寫後會自動從 authorities 抓取 role
                String accessToken = tokenProvider.generateAccessToken(authentication);
                String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

                // 在 console 顯示登入者的 JWT（測試用）
                System.out.println("========== LOGIN SUCCESS ==========");
                System.out.println("User: " + user.getEmail());
                System.out.println("Access Token: " + accessToken);
                System.out.println("Refresh Token: " + refreshToken);
                System.out.println("===================================");

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(accessTokenExpirationMs / 1000) // in seconds
                                .user(mapToUserResponse(user))
                                .build();
        }

        /**
         * 驗證 2FA 驗證碼
         */
        private boolean verify2FACode(String secret, String code) {
                try {
                        com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
                        int codeInt = Integer.parseInt(code);
                        return gAuth.authorize(secret, codeInt);
                } catch (NumberFormatException e) {
                        return false;
                }
        }

        public AuthResponse refreshToken(String refreshToken) {
                if (!tokenProvider.validateToken(refreshToken)) {
                        throw new RuntimeException("Invalid refresh token");
                }

                String email = tokenProvider.getEmailFromToken(refreshToken);
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String newAccessToken = tokenProvider.generateAccessToken(email,
                                Boolean.TRUE.equals(user.getIsStore()) ? "STORE" : "USER");
                String newRefreshToken = tokenProvider.generateRefreshToken(email);

                return AuthResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken)
                                .tokenType("Bearer")
                                .expiresIn(accessTokenExpirationMs / 1000)
                                .user(mapToUserResponse(user))
                                .build();
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
