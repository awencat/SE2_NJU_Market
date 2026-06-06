package com.marketback.main.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marketback.main.dto.PurchaseRequest;
import com.marketback.main.dto.PurchaseResponse;
import com.marketback.main.entity.OrderEntity;

import java.util.List;

public interface OrderEntityService extends IService<OrderEntity> {

    PurchaseResponse purchase(PurchaseRequest request);

    OrderEntity cancel(Integer orderId, Integer buyerId);

    OrderEntity complete(Integer orderId, Integer sellerId);

    List<OrderEntity> listBySeller(Integer sellerId);

    List<OrderEntity> listByBuyer(Integer buyerId);
}
