package com.marketback.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AvatarStorageProperties avatarStorageProperties;
    private final GoodsImageStorageProperties goodsImageStorageProperties;

    public WebMvcConfig(AvatarStorageProperties avatarStorageProperties,
                        GoodsImageStorageProperties goodsImageStorageProperties) {
        this.avatarStorageProperties = avatarStorageProperties;
        this.goodsImageStorageProperties = goodsImageStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPath = ensureTrailingSlash(avatarStorageProperties.getPublicPath());
        Path uploadPath = Paths.get(avatarStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        registry.addResourceHandler(publicPath + "**")
                .addResourceLocations(uploadPath.toUri().toString());

        String goodsImagePublicPath = ensureTrailingSlash(goodsImageStorageProperties.getPublicPath());
        Path goodsImagePath = Paths.get(goodsImageStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        registry.addResourceHandler(goodsImagePublicPath + "**")
                .addResourceLocations(goodsImagePath.toUri().toString());
    }

    private String ensureTrailingSlash(String value) {
        return value.endsWith("/") ? value : value + "/";
    }
}

