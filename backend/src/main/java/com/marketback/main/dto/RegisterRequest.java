package com.marketback.main.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,20}$", message = "username must be 3-20 chars and contain only letters, numbers or underscore")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 64, message = "password length must be 6-64")
    private String password;

    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    @Size(max = 120, message = "email is too long")
    private String email;

    @Pattern(regexp = "^$|^[0-9+\\-]{6,30}$", message = "phone format is invalid")
    private String phone;

    @Size(max = 100, message = "campus is too long")
    private String campus;

    @Size(max = 255, message = "address is too long")
    private String address;
}
