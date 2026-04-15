package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.GoodImage;
import com.marketback.main.mapper.GoodImageMapper;
import com.marketback.main.service.GoodImageService;
import org.springframework.stereotype.Service;

@Service
public class GoodImageServiceImpl extends ServiceImpl<GoodImageMapper, GoodImage> implements GoodImageService {
}
