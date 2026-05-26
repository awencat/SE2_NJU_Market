package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.Good;
import com.marketback.main.mapper.GoodMapper;
import com.marketback.main.service.GoodImageService;
import com.marketback.main.service.GoodService;
import org.springframework.stereotype.Service;

@Service
public class GoodServiceImpl extends ServiceImpl<GoodMapper, Good> implements GoodService {

    private final GoodImageService goodImageService;

    public GoodServiceImpl(GoodImageService goodImageService) {
        this.goodImageService = goodImageService;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        if (id instanceof Integer goodId) {
            goodImageService.deleteImagesByGoodId(goodId);
        }
        return super.removeById(id);
    }
}
