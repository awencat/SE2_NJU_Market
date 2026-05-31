package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.dto.AdminAuthResponse;
import com.marketback.main.dto.LoginRequest;
import com.marketback.main.entity.Admin;
import com.marketback.main.mapper.AdminMapper;
import com.marketback.main.service.AdminService;
import com.marketback.main.util.InputSanitizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminAuthResponse login(LoginRequest request) {
        String username = InputSanitizer.cleanPlainText(request.getUsername());
        Admin admin = getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username)
                .last("LIMIT 1"));
        if (admin == null || admin.getPassword() == null || !passwordMatches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("admin username or password is incorrect");
        }
        return new AdminAuthResponse(admin.getAdminId(), admin.getUsername(), admin.getRole());
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }
}
