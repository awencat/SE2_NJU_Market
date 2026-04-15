package com.marketback.main.controller;

import com.marketback.main.entity.Admin;
import com.marketback.main.service.AdminService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
public class AdminController extends BaseCrudController<Admin> {

    public AdminController(AdminService adminService) {
        super(adminService, "adminId");
    }
}
