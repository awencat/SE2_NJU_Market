package com.marketback.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminAuthResponse {

    private Integer adminId;

    private String username;

    private String role;
}
