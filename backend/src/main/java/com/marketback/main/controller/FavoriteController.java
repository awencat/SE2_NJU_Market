package com.marketback.main.controller;

import com.marketback.main.entity.Favorite;
import com.marketback.main.service.FavoriteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController extends BaseCrudController<Favorite> {

    public FavoriteController(FavoriteService favoriteService) {
        super(favoriteService, "favoriteId");
    }
}
