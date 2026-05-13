package com.marketback.main.controller;

import com.marketback.main.common.ApiResponse;
import com.marketback.main.entity.User;
import com.marketback.main.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseCrudController<User> {

    private final UserService userService;

    public UserController(UserService userService) {
        super(userService, "userId", User.class);
        this.userService = userService;
    }

    @PostMapping("/{id}/avatar")
    public ApiResponse<User> uploadAvatar(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("avatar uploaded", userService.uploadAvatar(id, file));
    }
}
