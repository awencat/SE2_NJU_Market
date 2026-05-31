package com.marketback.main.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marketback.main.dto.AdminAuthResponse;
import com.marketback.main.dto.LoginRequest;
import com.marketback.main.entity.Admin;

public interface AdminService extends IService<Admin> {

    AdminAuthResponse login(LoginRequest request);
}
