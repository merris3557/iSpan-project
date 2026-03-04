package com.example.demo.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AdminPasswordResetTokenRepository extends JpaRepository<AdminPasswordResetToken, Long> {

    Optional<AdminPasswordResetToken> findByToken(String token);

    Optional<AdminPasswordResetToken> findByAdmin(Admin admin);

    @Modifying
    @Query("DELETE FROM AdminPasswordResetToken t WHERE t.expiryDate <= :now")
    void deleteExpiredTokens(LocalDateTime now);
}
