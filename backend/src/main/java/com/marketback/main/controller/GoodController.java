package com.marketback.main.controller;

import com.marketback.main.entity.Good;
import com.marketback.main.service.GoodService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goods")
public class GoodController extends BaseCrudController<Good> {

    public GoodController(GoodService goodService) {
        super(goodService, "goodId");
    }
}
