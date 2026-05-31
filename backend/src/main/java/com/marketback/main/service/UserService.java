package com.marketback.main.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marketback.main.dto.AuthResponse;
import com.marketback.main.dto.LoginRequest;
import com.marketback.main.dto.RegisterRequest;
import com.marketback.main.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    User uploadAvatar(Integer userId, MultipartFile file);

    User banUser(Integer userId);

    User unbanUser(Integer userId);
}
