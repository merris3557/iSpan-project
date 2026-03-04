package com.example.demo.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByAccount(String account);

    Optional<Admin> findByEmail(String email);

    boolean existsByAccount(String account);
}
