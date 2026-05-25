package com.marketback.main.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.marketback.main.common.ApiResponse;
import com.marketback.main.dto.RatingSummary;
import com.marketback.main.entity.Rating;
import com.marketback.main.service.RatingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController extends BaseCrudController<Rating> {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        super(ratingService, "ratingId", Rating.class);
        this.ratingService = ratingService;
    }

    @GetMapping("/good/{goodId}")
    public ApiResponse<List<Rating>> listByGood(@PathVariable Integer goodId) {
        List<Rating> ratings = ratingService.list(new LambdaQueryWrapper<Rating>()
                .eq(Rating::getGoodId, goodId)
                .orderByDesc(Rating::getCreatedAt));
        return ApiResponse.success(ratings);
    }

    @GetMapping("/good/{goodId}/summary")
    public ApiResponse<RatingSummary> summaryByGood(@PathVariable Integer goodId) {
        List<Rating> ratings = ratingService.list(new LambdaQueryWrapper<Rating>()
                .eq(Rating::getGoodId, goodId));
        double average = ratings.stream()
                .map(Rating::getScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0D);
        return ApiResponse.success(new RatingSummary(goodId, Math.round(average * 10) / 10.0, (long) ratings.size()));
    }
}
