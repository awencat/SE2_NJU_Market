package com.marketback.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "username is required")
    @Size(max = 50, message = "username is too long")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 64, message = "password length is invalid")
    private String password;
}
