package com.marketback.main.controller;

import com.marketback.main.entity.User;
import com.marketback.main.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseCrudController<User> {

    public UserController(UserService userService) {
        super(userService, "userId");
    }
}
