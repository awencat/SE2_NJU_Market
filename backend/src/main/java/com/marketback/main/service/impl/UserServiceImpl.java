package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.config.AvatarStorageProperties;
import com.marketback.main.dto.AuthResponse;
import com.marketback.main.dto.LoginRequest;
import com.marketback.main.dto.RegisterRequest;
import com.marketback.main.entity.User;
import com.marketback.main.mapper.UserMapper;
import com.marketback.main.service.UserService;
import com.marketback.main.util.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_BANNED = "BANNED";

    private final PasswordEncoder passwordEncoder;
    private final AvatarStorageProperties avatarStorageProperties;

    public UserServiceImpl(PasswordEncoder passwordEncoder, AvatarStorageProperties avatarStorageProperties) {
        this.passwordEncoder = passwordEncoder;
        this.avatarStorageProperties = avatarStorageProperties;
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
        if (user == null || user.getPassword() == null || !passwordMatches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("username or password is incorrect");
        }
        if (STATUS_BANNED.equals(user.getStatus())) {
            throw new IllegalArgumentException("user is banned");
        }
        return toAuthResponse(user);
    }

    @Override
    public User uploadAvatar(Integer userId, MultipartFile file) {//头像传到后端文件夹
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("avatar file is required");
        }

        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        String fileName = "avatar_" + userId + "_" + Instant.now().toEpochMilli() + "_"
                + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path uploadDir = Paths.get(avatarStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path targetFile = uploadDir.resolve(fileName).normalize();
        if (!targetFile.startsWith(uploadDir)) {
            throw new IllegalArgumentException("invalid avatar path");
        }

        String oldAvatarUrl = user.getAvatarUrl();
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(targetFile);
            user.setAvatarUrl(ensureTrailingSlash(avatarStorageProperties.getPublicPath()) + fileName);
            if (!updateById(user)) {
                Files.deleteIfExists(targetFile);
                throw new IllegalArgumentException("avatar update failed");
            }
            deleteOldLocalAvatar(oldAvatarUrl, fileName);
            return user;
        } catch (IOException exception) {
            throw new IllegalArgumentException("avatar upload failed");
        }
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

    @Override
    public User banUser(Integer userId) {
        return updateUserStatus(userId, STATUS_BANNED);
    }

    @Override
    public User unbanUser(Integer userId) {
        return updateUserStatus(userId, STATUS_ACTIVE);
    }

    private User updateUserStatus(Integer userId, String status) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        user.setStatus(status);
        if (!updateById(user)) {
            throw new IllegalArgumentException("user status update failed");
        }
        return user;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
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
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(STATUS_ACTIVE);
        }
        if (entity.getPassword() != null && !entity.getPassword().isBlank() && !entity.getPassword().startsWith("$2")) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getCampus(),
                user.getAvatarUrl()
        );
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("avatar file extension is required");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("unsupported avatar file type");
        }
        return extension;
    }

    private void deleteOldLocalAvatar(String oldAvatarUrl, String newFileName) throws IOException {
        String publicPath = ensureTrailingSlash(avatarStorageProperties.getPublicPath());
        if (oldAvatarUrl == null || oldAvatarUrl.isBlank() || !oldAvatarUrl.startsWith(publicPath)) {
            return;
        }

        String oldFileName = oldAvatarUrl.substring(publicPath.length());
        if (oldFileName.isBlank() || oldFileName.equals(newFileName) || oldFileName.contains("/") || oldFileName.contains("\\")) {
            return;
        }

        Path uploadDir = Paths.get(avatarStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path oldFile = uploadDir.resolve(oldFileName).normalize();
        if (oldFile.startsWith(uploadDir)) {
            Files.deleteIfExists(oldFile);
        }
    }

    private String ensureTrailingSlash(String value) {
        return value.endsWith("/") ? value : value + "/";
    }
}
