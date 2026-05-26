package com.marketback.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AvatarStorageProperties avatarStorageProperties;

    public WebMvcConfig(AvatarStorageProperties avatarStorageProperties) {
        this.avatarStorageProperties = avatarStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPath = ensureTrailingSlash(avatarStorageProperties.getPublicPath());
        Path uploadPath = Paths.get(avatarStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        registry.addResourceHandler(publicPath + "**")
                .addResourceLocations(uploadPath.toUri().toString());

        Path goodsImagePath = Paths.get("uploads/goods").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/goods/**")
                .addResourceLocations(goodsImagePath.toUri().toString());
    }

    private String ensureTrailingSlash(String value) {
        return value.endsWith("/") ? value : value + "/";
    }
}

