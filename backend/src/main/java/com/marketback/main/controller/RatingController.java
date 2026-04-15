package com.marketback.main.controller;

import com.marketback.main.entity.Rating;
import com.marketback.main.service.RatingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ratings")
public class RatingController extends BaseCrudController<Rating> {

    public RatingController(RatingService ratingService) {
        super(ratingService, "ratingId");
    }
}
