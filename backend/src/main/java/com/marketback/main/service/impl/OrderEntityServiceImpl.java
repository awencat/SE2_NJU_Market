package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.dto.PurchaseRequest;
import com.marketback.main.dto.PurchaseResponse;
import com.marketback.main.entity.Good;
import com.marketback.main.entity.OrderEntity;
import com.marketback.main.entity.OrderItem;
import com.marketback.main.entity.User;
import com.marketback.main.mapper.OrderEntityMapper;
import com.marketback.main.service.GoodService;
import com.marketback.main.service.OrderEntityService;
import com.marketback.main.service.OrderItemService;
import com.marketback.main.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderEntityServiceImpl extends ServiceImpl<OrderEntityMapper, OrderEntity> implements OrderEntityService {

    private static final String STATUS_ON_SALE = "ON_SALE";
    private static final String STATUS_RESERVED = "RESERVED";
    private static final String STATUS_PENDING = "PENDING";

    private final GoodService goodService;
    private final OrderItemService orderItemService;
    private final UserService userService;

    public OrderEntityServiceImpl(GoodService goodService, OrderItemService orderItemService, UserService userService) {
        this.goodService = goodService;
        this.orderItemService = orderItemService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public PurchaseResponse purchase(PurchaseRequest request) {
        User buyer = userService.getById(request.getBuyerId());
        if (buyer == null) {
            throw new IllegalArgumentException("buyer not found");
        }

        Good good = goodService.getById(request.getGoodId());
        if (good == null) {
            throw new IllegalArgumentException("good not found");
        }
        if (request.getBuyerId().equals(good.getSellerId())) {
            throw new IllegalArgumentException("buyer cannot purchase own good");
        }
        if (good.getStatus() != null && !STATUS_ON_SALE.equals(good.getStatus())) {
            throw new IllegalArgumentException("good is not available");
        }

        User seller = userService.getById(good.getSellerId());
        if (seller == null) {
            throw new IllegalArgumentException("seller not found");
        }

        OrderEntity order = new OrderEntity();
        order.setBuyerId(request.getBuyerId());
        order.setSellerId(good.getSellerId());
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(good.getPrice());
        order.setStatus(STATUS_PENDING);
        save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getOrderId());
        item.setGoodId(good.getGoodId());
        item.setQuantity(1);
        item.setUnitPrice(good.getPrice());
        item.setSubtotal(good.getPrice());
        orderItemService.save(item);

        good.setStatus(STATUS_RESERVED);
        goodService.updateById(good);

        return new PurchaseResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getStatus(),
                good.getGoodId(),
                good.getTitle(),
                order.getTotalAmount(),
                seller.getUserId(),
                seller.getUsername(),
                seller.getPhone(),
                seller.getEmail()
        );
    }

    @Override
    @Transactional
    public OrderEntity cancel(Integer orderId, Integer buyerId) {
        OrderEntity order = getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("order not found");
        }
        if (buyerId != null && !buyerId.equals(order.getBuyerId())) {
            throw new IllegalArgumentException("only buyer can cancel this order");
        }

        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );

        for (OrderItem item : items) {
            Good good = goodService.getById(item.getGoodId());
            if (good != null) {
                good.setStatus(STATUS_ON_SALE);
                goodService.updateById(good);
            }
        }

        if (!items.isEmpty()) {
            orderItemService.remove(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        }

        if (!removeById(orderId)) {
            throw new IllegalArgumentException("order cancel failed");
        }

        return order;
    }

    @Override
    public List<OrderEntity> listBySeller(Integer sellerId) {
        return list(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getSellerId, sellerId)
                .orderByDesc(OrderEntity::getCreatedAt));
    }

    @Override
    public List<OrderEntity> listByBuyer(Integer buyerId) {
        return list(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getBuyerId, buyerId)
                .orderByDesc(OrderEntity::getCreatedAt));
    }

    private String generateOrderNumber() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
