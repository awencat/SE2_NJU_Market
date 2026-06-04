package com.marketback.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PurchaseResponse {

    private Integer orderId;
    private String orderNumber;
    private String status;
    private Integer goodId;
    private String goodTitle;
    private Integer count;
    private BigDecimal totalAmount;
    private Integer sellerId;
    private String sellerName;
    private String sellerPhone;
    private String sellerEmail;
}
