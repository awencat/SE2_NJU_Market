package com.marketback.main.controller;

import com.marketback.main.entity.OrderItem;
import com.marketback.main.service.OrderItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController extends BaseCrudController<OrderItem> {

    public OrderItemController(OrderItemService orderItemService) {
        super(orderItemService, "itemId");
    }
}
