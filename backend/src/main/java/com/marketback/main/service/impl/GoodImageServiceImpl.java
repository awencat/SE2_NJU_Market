package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.config.GoodsImageStorageProperties;
import com.marketback.main.entity.GoodImage;
import com.marketback.main.mapper.GoodMapper;
import com.marketback.main.mapper.GoodImageMapper;
import com.marketback.main.service.GoodImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodImageServiceImpl extends ServiceImpl<GoodImageMapper, GoodImage> implements GoodImageService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final GoodsImageStorageProperties goodsImageStorageProperties;
    private final GoodMapper goodMapper;

    public GoodImageServiceImpl(GoodsImageStorageProperties goodsImageStorageProperties, GoodMapper goodMapper) {
        this.goodsImageStorageProperties = goodsImageStorageProperties;
        this.goodMapper = goodMapper;
    }

    @Override
    public List<GoodImage> uploadImages(Integer goodId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("image files are required");
        }
        if (goodMapper.selectById(goodId) == null) {
            throw new IllegalArgumentException("good not found");
        }

        List<GoodImage> uploadedImages = new ArrayList<>();
        long existingCount = count(new LambdaQueryWrapper<GoodImage>().eq(GoodImage::getGoodId, goodId));

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String extension = resolveExtension(file.getOriginalFilename());
            String fileName = "good_" + goodId + "_" + Instant.now().toEpochMilli() + "_"
                    + UUID.randomUUID().toString().replace("-", "") + "." + extension;

            Path uploadDir = Paths.get(goodsImageStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            Path targetFile = uploadDir.resolve(fileName).normalize();

            if (!targetFile.startsWith(uploadDir)) {
                throw new IllegalArgumentException("invalid image path");
            }

            try {
                Files.createDirectories(uploadDir);
                file.transferTo(targetFile);

                GoodImage goodImage = new GoodImage();
                goodImage.setGoodId(goodId);
                goodImage.setImageUrl(ensureTrailingSlash(goodsImageStorageProperties.getPublicPath()) + fileName);
                goodImage.setSortOrder(Math.toIntExact(existingCount + uploadedImages.size()));

                if (save(goodImage)) {
                    uploadedImages.add(goodImage);
                } else {
                    Files.deleteIfExists(targetFile);
                    throw new IllegalArgumentException("image save failed");
                }
            } catch (IOException exception) {
                throw new IllegalArgumentException("image upload failed: " + exception.getMessage());
            }
        }

        return uploadedImages;
    }

    @Override
    public List<GoodImage> listByGoodId(Integer goodId) {
        return list(new LambdaQueryWrapper<GoodImage>()
                .eq(GoodImage::getGoodId, goodId)
                .orderByAsc(GoodImage::getSortOrder)
                .orderByAsc(GoodImage::getImageId));
    }

    @Override
    public GoodImage updateImage(Integer imageId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image file is required");
        }

        GoodImage existingImage = getById(imageId);
        if (existingImage == null) {
            throw new IllegalArgumentException("image not found");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        String fileName = "good_" + existingImage.getGoodId() + "_" + Instant.now().toEpochMilli() + "_"
                + UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path uploadDir = Paths.get(goodsImageStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path targetFile = uploadDir.resolve(fileName).normalize();

        if (!targetFile.startsWith(uploadDir)) {
            throw new IllegalArgumentException("invalid image path");
        }

        String oldImageUrl = existingImage.getImageUrl();
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(targetFile);

            existingImage.setImageUrl(ensureTrailingSlash(goodsImageStorageProperties.getPublicPath()) + fileName);

            if (!updateById(existingImage)) {
                Files.deleteIfExists(targetFile);
                throw new IllegalArgumentException("image update failed");
            }

            deleteOldLocalImage(oldImageUrl, fileName);

            return existingImage;
        } catch (IOException exception) {
            throw new IllegalArgumentException("image update failed: " + exception.getMessage());
        }
    }

    @Override
    public void deleteImage(Integer imageId) {
        GoodImage image = getById(imageId);
        if (image == null) {
            return;
        }

        if (removeById(imageId)) {
            deleteLocalImage(image.getImageUrl());
        }
    }

    @Override
    public void deleteImagesByGoodId(Integer goodId) {
        List<GoodImage> images = listByGoodId(goodId);
        if (images.isEmpty()) {
            return;
        }

        List<Integer> imageIds = images.stream()
                .map(GoodImage::getImageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!imageIds.isEmpty()) {
            removeByIds(imageIds);
        }
        images.forEach(image -> deleteLocalImage(image.getImageUrl()));
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("image file extension is required");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("unsupported image file type");
        }
        return extension;
    }

    private void deleteOldLocalImage(String oldImageUrl, String newFileName) throws IOException {
        String publicPath = ensureTrailingSlash(goodsImageStorageProperties.getPublicPath());
        if (oldImageUrl == null || oldImageUrl.isBlank() || !oldImageUrl.startsWith(publicPath)) {
            return;
        }

        String oldFileName = oldImageUrl.substring(publicPath.length());
        if (oldFileName.isBlank() || oldFileName.equals(newFileName) || oldFileName.contains("/") || oldFileName.contains("\\")) {
            return;
        }

        Path uploadDir = Paths.get(goodsImageStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path oldFile = uploadDir.resolve(oldFileName).normalize();
        if (oldFile.startsWith(uploadDir)) {
            Files.deleteIfExists(oldFile);
        }
    }

    private void deleteLocalImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String publicPath = ensureTrailingSlash(goodsImageStorageProperties.getPublicPath());
        if (!imageUrl.startsWith(publicPath)) {
            return;
        }

        String fileName = imageUrl.substring(publicPath.length());
        if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
            return;
        }

        try {
            Path uploadDir = Paths.get(goodsImageStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            Path file = uploadDir.resolve(fileName).normalize();
            if (file.startsWith(uploadDir)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            // 记录日志但不抛出异常，避免影响主流程
            System.err.println("Failed to delete local image file: " + e.getMessage());
        }
    }

    private String ensureTrailingSlash(String value) {
        return value.endsWith("/") ? value : value + "/";
    }
}

