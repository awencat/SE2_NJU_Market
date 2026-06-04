package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.dto.RatingSummary;
import com.marketback.main.entity.OrderEntity;
import com.marketback.main.entity.OrderItem;
import com.marketback.main.entity.Rating;
import com.marketback.main.mapper.RatingMapper;
import com.marketback.main.service.OrderEntityService;
import com.marketback.main.service.OrderItemService;
import com.marketback.main.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating> implements RatingService {

    private final OrderEntityService orderEntityService;
    private final OrderItemService orderItemService;

    public RatingServiceImpl(OrderEntityService orderEntityService, OrderItemService orderItemService) {
        this.orderEntityService = orderEntityService;
        this.orderItemService = orderItemService;
    }

    @Override
    public boolean save(Rating entity) {
        if (entity == null) {
            throw new IllegalArgumentException("rating is required");
        }
        Integer score = entity.getScore();
        if (score == null || score < 1 || score > 5) {
            throw new IllegalArgumentException("score must be between 1 and 5");
        }
        validatePurchased(entity.getUserId(), entity.getGoodId());
        boolean exists = count(new LambdaQueryWrapper<Rating>()
                .eq(Rating::getUserId, entity.getUserId())
                .eq(Rating::getGoodId, entity.getGoodId())) > 0;
        if (exists) {
            throw new IllegalArgumentException("user already rated this good");
        }
        return super.save(entity);
    }

    public List<Rating> listByGood(Integer goodId) {
        return list(new LambdaQueryWrapper<Rating>()
                .eq(Rating::getGoodId, goodId)
                .orderByDesc(Rating::getCreatedAt));
    }

    public RatingSummary summaryByGood(Integer goodId) {
        List<Rating> ratings = listByGood(goodId);
        double average = ratings.stream()
                .map(Rating::getScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0D);
        return new RatingSummary(goodId, Math.round(average * 10) / 10.0, (long) ratings.size());
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
            throw new IllegalArgumentException("only buyer can rate this good");
        }
    }
}
