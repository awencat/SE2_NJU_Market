package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.Good;
import com.marketback.main.mapper.GoodMapper;
import com.marketback.main.service.GoodService;
import org.springframework.stereotype.Service;

@Service
public class GoodServiceImpl extends ServiceImpl<GoodMapper, Good> implements GoodService {
}
