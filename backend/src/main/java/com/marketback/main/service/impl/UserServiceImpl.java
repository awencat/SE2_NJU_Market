package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.dto.AuthResponse;
import com.marketback.main.dto.LoginRequest;
import com.marketback.main.dto.RegisterRequest;
import com.marketback.main.entity.User;
import com.marketback.main.mapper.UserMapper;
import com.marketback.main.service.UserService;
import com.marketback.main.util.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = InputSanitizer.cleanPlainText(request.getUsername());
        InputSanitizer.ensureSafeIdentifier(username);

        boolean exists = lambdaQuery()
                .eq(User::getUsername, username)
                .or()
                .eq(User::getEmail, request.getEmail().trim())
                .exists();
        if (exists) {
            throw new IllegalArgumentException("username or email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(InputSanitizer.cleanPlainText(request.getEmail()));
        user.setPhone(InputSanitizer.normalizeNullableText(request.getPhone()));
        user.setCampus(InputSanitizer.normalizeNullableText(request.getCampus()));
        user.setAddress(InputSanitizer.normalizeNullableText(request.getAddress()));

        if (!save(user)) {
            throw new IllegalArgumentException("register failed");
        }
        return toAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = InputSanitizer.cleanPlainText(request.getUsername());
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("LIMIT 1"));
        if (user == null || user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("username or password is incorrect");
        }
        return toAuthResponse(user);
    }

    @Override
    public boolean save(User entity) {
        normalizeUser(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(User entity) {
        normalizeUser(entity);
        return super.updateById(entity);
    }

    private void normalizeUser(User entity) {
        if (entity == null) {
            return;
        }
        if (entity.getUsername() != null) {
            String username = InputSanitizer.cleanPlainText(entity.getUsername());
            InputSanitizer.ensureSafeIdentifier(username);
            entity.setUsername(username);
        }
        if (entity.getEmail() != null) {
            entity.setEmail(InputSanitizer.cleanPlainText(entity.getEmail()));
        }
        entity.setPhone(InputSanitizer.normalizeNullableText(entity.getPhone()));
        entity.setCampus(InputSanitizer.normalizeNullableText(entity.getCampus()));
        entity.setAddress(InputSanitizer.normalizeNullableText(entity.getAddress()));
        if (entity.getPassword() != null && !entity.getPassword().isBlank() && !entity.getPassword().startsWith("$2")) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getCampus()
        );
    }
}
