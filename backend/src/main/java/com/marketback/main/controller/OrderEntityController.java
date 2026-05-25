package com.marketback.main.controller;

import com.marketback.main.common.ApiResponse;
import com.marketback.main.dto.PurchaseRequest;
import com.marketback.main.dto.PurchaseResponse;
import com.marketback.main.entity.OrderEntity;
import com.marketback.main.service.OrderEntityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderEntityController extends BaseCrudController<OrderEntity> {

    private final OrderEntityService orderEntityService;

    public OrderEntityController(OrderEntityService orderEntityService) {
        super(orderEntityService, "orderId", OrderEntity.class);
        this.orderEntityService = orderEntityService;
    }

    @PostMapping("/purchase")
    public ApiResponse<PurchaseResponse> purchase(@Valid @RequestBody PurchaseRequest request) {
        return ApiResponse.success("purchased", orderEntityService.purchase(request));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderEntity> cancel(
            @PathVariable Integer orderId,
            @RequestParam(value = "buyerId", required = false) Integer buyerId) {
        return ApiResponse.success("cancelled", orderEntityService.cancel(orderId, buyerId));
    }

    @GetMapping("/seller/{sellerId}")
    public ApiResponse<List<OrderEntity>> listBySeller(@PathVariable Integer sellerId) {
        return ApiResponse.success(orderEntityService.listBySeller(sellerId));
    }

    @GetMapping("/buyer/{buyerId}")
    public ApiResponse<List<OrderEntity>> listByBuyer(@PathVariable Integer buyerId) {
        return ApiResponse.success(orderEntityService.listByBuyer(buyerId));
    }
}
