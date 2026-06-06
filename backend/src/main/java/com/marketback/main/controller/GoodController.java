package com.marketback.main.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marketback.main.common.ApiResponse;
import com.marketback.main.controller.QueryPageParam;
import com.marketback.main.entity.Good;
import com.marketback.main.entity.GoodImage;
import com.marketback.main.entity.User;
import com.marketback.main.service.GoodImageService;
import com.marketback.main.service.GoodService;
import com.marketback.main.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/goods")
public class GoodController extends BaseCrudController<Good> {

    private final GoodService goodService;
    private final UserService userService;
    private final GoodImageService goodImageService;

    public GoodController(GoodService goodService, UserService userService, GoodImageService goodImageService) {
        super(goodService, "goodId", Good.class);
        this.goodService = goodService;
        this.userService = userService;
        this.goodImageService = goodImageService;
    }

    @PostMapping
    public ApiResponse<Good> create(@RequestBody Good good) {
        normalizeCategory(good);
        return super.create(good);
    }

    @PutMapping("/{id}")
    public ApiResponse<Good> update(@PathVariable Integer id, @RequestBody Good good) {
        normalizeCategory(good);
        return super.update(id, good);
    }

    @PostMapping("/listPage")
    public ApiResponse<Map<String, Object>> listPage(@RequestBody QueryPageParam query) {
        Map<String, Object> param = query.getParam();
        Integer goodId = (Integer) param.get("goodId");
        String title = (String) param.get("title");
        String category = (String) param.get("category");
        Integer sellerId = (Integer) param.get("sellerId");

        Page<Good> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Good> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(title) && !"null".equals(title)) {
            wrapper.like(Good::getTitle, title);
        }

        if (StringUtils.isNotBlank(category) && !"null".equals(category)) {
            wrapper.in(Good::getCategory, categoryAliases(category));
        }

        if (goodId != null) {
            wrapper.eq(Good::getGoodId, goodId);
        }

        if (sellerId != null && sellerId > 0) {
            wrapper.eq(Good::getSellerId, sellerId);
        }
        wrapper.orderByDesc(Good::getCreatedAt);

        IPage<Good> result = goodService.page(page, wrapper);
        List<Good> records = result.getRecords();

        Set<Integer> sellerIds = records.stream()
                .map(Good::getSellerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, User> userMap = new HashMap<>();
        if (!sellerIds.isEmpty()) {
            List<User> users = userService.listByIds(sellerIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getUserId, u -> u));
        }

        for (Good good : records) {
            if (good.getSellerId() != null) {
                User seller = userMap.get(good.getSellerId());
                if (seller != null) {
                    good.setSellerName(seller.getUsername());
                    good.setSellerPhone(seller.getPhone());
                    good.setSellerEmail(seller.getEmail());
                } else {
                    good.setSellerName("未知用户");
                }
            }
            List<GoodImage> images = goodImageService.listByGoodId(good.getGoodId());
            good.setImages(images);
            if (!images.isEmpty()) {
                String coverUrl = images.get(0).getImageUrl();
                good.setImageUrl(coverUrl);
                good.setImage(coverUrl);
                good.setCoverUrl(coverUrl);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", result.getTotal());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());

        return ApiResponse.success(response);
    }

    private List<String> categoryAliases(String category) {
        Map<String, List<String>> aliases = Map.of(
                "daily", List.of("daily", "DAILY", "日用百货", "日用商城"),
                "digital", List.of("digital", "DIGITAL", "数码设备", "数码商城"),
                "book", List.of("book", "BOOK", "教材书刊", "书刊", "书刊商城", "图书"),
                "sports", List.of("sports", "SPORTS", "体育用品", "体育商城"),
                "pet", List.of("pet", "PET", "宠物用品", "宠物商城")
        );
        return aliases.getOrDefault(normalizeCategoryValue(category), List.of(category));
    }

    private void normalizeCategory(Good good) {
        if (good != null) {
            good.setCategory(normalizeCategoryValue(good.getCategory()));
        }
    }

    private String normalizeCategoryValue(String category) {
        if (category == null) {
            return null;
        }
        String value = category.trim();
        Map<String, String> normalized = Map.ofEntries(
                Map.entry("DAILY", "daily"),
                Map.entry("日用百货", "daily"),
                Map.entry("日用商城", "daily"),
                Map.entry("DIGITAL", "digital"),
                Map.entry("数码设备", "digital"),
                Map.entry("数码商城", "digital"),
                Map.entry("BOOK", "book"),
                Map.entry("教材书刊", "book"),
                Map.entry("书刊", "book"),
                Map.entry("书刊商城", "book"),
                Map.entry("图书", "book"),
                Map.entry("SPORTS", "sports"),
                Map.entry("体育用品", "sports"),
                Map.entry("体育商城", "sports"),
                Map.entry("PET", "pet"),
                Map.entry("宠物用品", "pet"),
                Map.entry("宠物商城", "pet")
        );
        return normalized.getOrDefault(value, value.toLowerCase(Locale.ROOT));
    }
}

