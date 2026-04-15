package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.OrderEntity;
import com.marketback.main.mapper.OrderEntityMapper;
import com.marketback.main.service.OrderEntityService;
import org.springframework.stereotype.Service;

@Service
public class OrderEntityServiceImpl extends ServiceImpl<OrderEntityMapper, OrderEntity> implements OrderEntityService {
}
