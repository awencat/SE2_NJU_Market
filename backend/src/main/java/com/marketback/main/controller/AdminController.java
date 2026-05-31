package com.marketback.main.controller;

import com.marketback.main.common.ApiResponse;
import com.marketback.main.dto.AdminAuthResponse;
import com.marketback.main.dto.LoginRequest;
import com.marketback.main.entity.Admin;
import com.marketback.main.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
public class AdminController extends BaseCrudController<Admin> {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        super(adminService, "adminId", Admin.class);
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("admin logged in", adminService.login(request));
    }
}
