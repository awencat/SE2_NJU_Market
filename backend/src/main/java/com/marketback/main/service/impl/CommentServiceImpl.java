package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.Comment;
import com.marketback.main.entity.OrderEntity;
import com.marketback.main.entity.OrderItem;
import com.marketback.main.mapper.CommentMapper;
import com.marketback.main.service.CommentService;
import com.marketback.main.service.OrderEntityService;
import com.marketback.main.service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final OrderEntityService orderEntityService;
    private final OrderItemService orderItemService;

    public CommentServiceImpl(OrderEntityService orderEntityService, OrderItemService orderItemService) {
        this.orderEntityService = orderEntityService;
        this.orderItemService = orderItemService;
    }

    @Override
    public boolean save(Comment entity) {
        validatePurchased(entity.getUserId(), entity.getGoodId());
        return super.save(entity);
    }

    public List<Comment> listByGood(Integer goodId) {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getGoodId, goodId)
                .orderByDesc(Comment::getCreatedAt));
    }

    private void validatePurchased(Integer userId, Integer goodId) {
        if (userId == null || goodId == null) {
            throw new IllegalArgumentException("userId and goodId are required");
        }
        boolean purchased = orderEntityService.list(new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getBuyerId, userId))
                .stream()
                .anyMatch(order -> orderItemService.count(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getOrderId())
                        .eq(OrderItem::getGoodId, goodId)) > 0);
        if (!purchased) {
            throw new IllegalArgumentException("only buyer can comment this good");
        }
    }
}
