package com.example.demo.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.demo.admin.AdminPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegistrationRequest {

    @NotBlank(message = "Account is required")
    private String account;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Position is required")
    private AdminPosition position;

    @NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;
}
