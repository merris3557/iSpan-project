package com.example.demo.admin;

import com.example.demo.admin.dto.AdminLoginRequest;
import com.example.demo.admin.dto.AdminLoginResponse;
import com.example.demo.admin.dto.AdminRegistrationRequest;
import com.example.demo.admin.dto.AdminResponse;
import com.example.demo.auth.LoginAttemptService;
import com.example.demo.common.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    public AdminLoginResponse login(AdminLoginRequest request) {
        String account = request.getAccount();

        // 0. 檢查是否已被防爆破機制鎖定
        if (loginAttemptService.isBlocked(account)) {
            throw new RuntimeException("登入失敗3次，請15分鐘後再試，或點選忘記密碼重設");
        }

        // 1. 根據 account 查詢 Admin
        Admin admin = adminRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("登入失敗，請重新嘗試"));

        // 2. 檢查帳號是否被停用
        if (!Boolean.TRUE.equals(admin.getEnabled())) {
            throw new RuntimeException("Account is disabled");
        }

        // 3. 驗證密碼
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            loginAttemptService.loginFailed(account);
            throw new RuntimeException("登入失敗，請重新嘗試");
        }

        // 登入成功，重置失敗計數
        loginAttemptService.loginSucceeded(account);

        // 4. 生成 JWT tokens (包含 role="ADMIN" 和 position)
        String accessToken = tokenProvider.generateAccessToken(
                admin.getAccount(),
                "ADMIN",
                admin.getPosition() != null ? admin.getPosition().name() : null);
        String refreshToken = tokenProvider.generateRefreshToken(admin.getAccount());

        // 5. 在 console 顯示登入資訊（測試用）
        System.out.println("========== ADMIN LOGIN SUCCESS ==========");
        System.out.println("Account: " + admin.getAccount());
        System.out.println("Name: " + admin.getName());
        System.out.println("Position: " + admin.getPosition());
        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);
        System.out.println("=========================================");

        // 6. 返回包含 JWT 和 admin 資訊的 response
        return AdminLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000) // in seconds
                .admin(mapToAdminResponse(admin))
                .build();
    }

    public AdminResponse registerAdmin(AdminRegistrationRequest request) {
        // 1. 檢查帳號是否已存在
        if (adminRepository.existsByAccount(request.getAccount())) {
            throw new RuntimeException("Account already exists: " + request.getAccount());
        }

        // 2. 建立 Admin 物件，密碼預設為帳號（BCrypt 加密）
        Admin admin = Admin.builder()
                .account(request.getAccount())
                .name(request.getName())
                .email(request.getEmail())
                .position(request.getPosition())
                .password(passwordEncoder.encode(request.getAccount())) // 密碼 = 帳號（加密）
                .enabled(true) // 預設啟用
                .build();

        // 3. 儲存至資料庫
        admin = adminRepository.save(admin);

        // 4. 在 console 顯示註冊資訊（測試用）
        System.out.println("========== ADMIN REGISTRATION SUCCESS ==========");
        System.out.println("Account: " + admin.getAccount());
        System.out.println("Name: " + admin.getName());
        System.out.println("Position: " + admin.getPosition());
        System.out.println("Default Password: " + request.getAccount() + " (encrypted)");
        System.out.println("===============================================");

        // 5. 回傳 AdminResponse
        return mapToAdminResponse(admin);
    }

    public AdminResponse updateAdmin(Integer id, com.example.demo.admin.dto.AdminUpdateRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));

        // 檢查帳號是否與其他人衝突
        adminRepository.findByAccount(request.getAccount())
                .ifPresent(existingAdmin -> {
                    if (!existingAdmin.getId().equals(id)) {
                        throw new RuntimeException("Account already exists: " + request.getAccount());
                    }
                });

        // 檢查信箱是否與其他人衝突
        adminRepository.findByEmail(request.getEmail())
                .ifPresent(existingAdmin -> {
                    if (!existingAdmin.getId().equals(id)) {
                        throw new RuntimeException("Email already exists: " + request.getEmail());
                    }
                });

        admin.setAccount(request.getAccount());
        admin.setName(request.getName());
        admin.setPosition(request.getPosition());
        admin.setEmail(request.getEmail());
        admin.setEnabled(request.getEnabled());

        admin = adminRepository.save(admin);

        System.out.println("========== ADMIN UPDATE SUCCESS ==========");
        System.out.println("ID: " + admin.getId());
        System.out.println("Account: " + admin.getAccount());
        System.out.println("Name: " + admin.getName());
        System.out.println("Position: " + admin.getPosition());
        System.out.println("Enabled: " + admin.getEnabled());
        System.out.println("=========================================");

        return mapToAdminResponse(admin);
    }

    public java.util.List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    private AdminResponse mapToAdminResponse(Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .account(admin.getAccount())
                .name(admin.getName())
                .email(admin.getEmail())
                .position(admin.getPosition())
                .enabled(admin.getEnabled())
                .build();
    }

    /**
     * 供 AdminController 公開呼叫（/me 端點使用）
     */
    public AdminResponse mapToPublicAdminResponse(Admin admin) {
        return mapToAdminResponse(admin);
    }

    /**
     * Refresh Token：驗證 refreshToken 後重新簽發 Access + Refresh Token
     */
    public AdminLoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String account = tokenProvider.getEmailFromToken(refreshToken); // subject 為 account
        Admin admin = adminRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Admin not found with account: " + account));

        if (!Boolean.TRUE.equals(admin.getEnabled())) {
            throw new RuntimeException("Account is disabled");
        }

        String newAccessToken = tokenProvider.generateAccessToken(
                admin.getAccount(),
                "ADMIN",
                admin.getPosition() != null ? admin.getPosition().name() : null);
        String newRefreshToken = tokenProvider.generateRefreshToken(admin.getAccount());

        return AdminLoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .admin(mapToAdminResponse(admin))
                .build();
    }

    public Admin getCurrentAdmin() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String account = authentication.getName();

        return adminRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Admin not found with account: " + account));
    }

    public AdminResponse getCurrentAdminResponse() {
        Admin currentAdmin = getCurrentAdmin();
        return mapToAdminResponse(currentAdmin);
    }
}
