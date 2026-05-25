package com.marketback.main.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseRequest {

    @NotNull(message = "buyerId is required")
    private Integer buyerId;

    @NotNull(message = "goodId is required")
    private Integer goodId;
}
