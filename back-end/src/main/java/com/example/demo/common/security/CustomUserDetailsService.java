package com.example.demo.common.security;

import com.example.demo.admin.Admin;
import com.example.demo.admin.AdminRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final AdminRepository adminRepository;

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                // First check User
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        return org.springframework.security.core.userdetails.User.builder()
                                        .username(user.getEmail())
                                        .password(user.getPassword())
                                        .authorities(Collections.singletonList(
                                                        new SimpleGrantedAuthority("ROLE_"
                                                                        + (Boolean.TRUE.equals(user.getIsStore())
                                                                                        ? "STORE"
                                                                                        : "USER"))))
                                        .accountExpired(false)
                                        .accountLocked(!user.getEnabled())
                                        .credentialsExpired(false)
                                        .disabled(!user.getEnabled())
                                        .build();
                }

                // If not found in User, check Admin
                Optional<Admin> adminOpt = adminRepository.findByAccount(email);
                if (adminOpt.isPresent()) {
                        Admin admin = adminOpt.get();
                        return org.springframework.security.core.userdetails.User.builder()
                                        .username(admin.getAccount())
                                        .password(admin.getPassword())
                                        .authorities(Collections
                                                        .singletonList(new SimpleGrantedAuthority("ROLE_" + admin.getPosition().name())))
                                        .accountExpired(false)
                                        .accountLocked(!admin.getEnabled())
                                        .credentialsExpired(false)
                                        .disabled(!admin.getEnabled())
                                        .build();
                }

                throw new UsernameNotFoundException("User or Admin not found with account/email: " + email);
        }
}
