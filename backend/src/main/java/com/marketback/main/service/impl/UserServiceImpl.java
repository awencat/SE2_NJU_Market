package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.User;
import com.marketback.main.mapper.UserMapper;
import com.marketback.main.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
