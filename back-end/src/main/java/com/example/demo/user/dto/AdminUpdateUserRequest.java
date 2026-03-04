package com.example.demo.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    @NotBlank(message = "Username/Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Store owner status is required")
    private Boolean isStore;

    @NotNull(message = "Account status is required")
    private Boolean enabled;
}
