package com.marketback.main.controller;

import com.marketback.main.entity.OrderEntity;
import com.marketback.main.service.OrderEntityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderEntityController extends BaseCrudController<OrderEntity> {

    public OrderEntityController(OrderEntityService orderEntityService) {
        super(orderEntityService, "orderId");
    }
}
