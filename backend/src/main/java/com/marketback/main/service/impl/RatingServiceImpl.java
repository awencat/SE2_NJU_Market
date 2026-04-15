package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.Rating;
import com.marketback.main.mapper.RatingMapper;
import com.marketback.main.service.RatingService;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating> implements RatingService {
}
