package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.OrderItem;
import com.marketback.main.mapper.OrderItemMapper;
import com.marketback.main.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}
