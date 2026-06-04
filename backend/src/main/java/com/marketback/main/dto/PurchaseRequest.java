package com.marketback.main.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PurchaseRequest {

    @NotNull(message = "buyerId is required")
    private Integer buyerId;

    @NotNull(message = "goodId is required")
    private Integer goodId;

    @NotNull(message = "count is required")
    @Min(value = 1, message = "count must be at least 1")
    private Integer count;
}
