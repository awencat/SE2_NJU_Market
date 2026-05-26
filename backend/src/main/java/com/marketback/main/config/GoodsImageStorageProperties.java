package com.marketback.main.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "market.goods-image")
public class GoodsImageStorageProperties {

    private String uploadDir = "uploads/goods";

    private String publicPath = "/uploads/goods/";

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public void setPublicPath(String publicPath) {
        this.publicPath = publicPath;
    }

    public String getUploadDir() {
        return uploadDir;
    }
    public String getPublicPath() {
        return publicPath;
    }
}
