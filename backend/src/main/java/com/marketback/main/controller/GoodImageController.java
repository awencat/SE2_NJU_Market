package com.marketback.main.controller;

import com.marketback.main.entity.GoodImage;
import com.marketback.main.service.GoodImageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/good-images")
public class GoodImageController extends BaseCrudController<GoodImage> {

    public GoodImageController(GoodImageService goodImageService) {
        super(goodImageService, "imageId");
    }
}
