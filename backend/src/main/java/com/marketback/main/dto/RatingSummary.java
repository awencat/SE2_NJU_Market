package com.marketback.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RatingSummary {

    private Integer goodId;

    private Double averageScore;

    private Long ratingCount;
}
