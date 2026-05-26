package com.marketback.main.controller;

import com.marketback.main.common.ApiResponse;
import com.marketback.main.entity.GoodImage;
import com.marketback.main.service.GoodImageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/good-images")
public class GoodImageController extends BaseCrudController<GoodImage> {

    private final GoodImageService goodImageService;

    public GoodImageController(GoodImageService goodImageService) {
        super(goodImageService, "imageId", GoodImage.class);
        this.goodImageService = goodImageService;
    }

    @PostMapping("/{goodId}/upload")
    public ApiResponse<List<GoodImage>> uploadImages(@PathVariable Integer goodId,
                                                     @RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success("images uploaded", goodImageService.uploadImages(goodId, files));
    }

    @GetMapping("/good/{goodId}")
    public ApiResponse<List<GoodImage>> listByGoodId(@PathVariable Integer goodId) {
        return ApiResponse.success(goodImageService.listByGoodId(goodId));
    }

    @PutMapping("/{imageId}/file")
    public ApiResponse<GoodImage> updateImage(@PathVariable Integer imageId,
                                              @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("image updated", goodImageService.updateImage(imageId, file));
    }

    @Override
    @DeleteMapping("/{imageId}")
    public ApiResponse<Boolean> delete(@PathVariable Integer imageId) {
        goodImageService.deleteImage(imageId);
        return ApiResponse.success("image deleted", true);
    }
}

