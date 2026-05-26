package com.marketback.main.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marketback.main.entity.GoodImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoodImageService extends IService<GoodImage> {

    List<GoodImage> uploadImages(Integer goodId, MultipartFile[] files);

    List<GoodImage> listByGoodId(Integer goodId);

    GoodImage updateImage(Integer imageId, MultipartFile file);

    void deleteImage(Integer imageId);

    void deleteImagesByGoodId(Integer goodId);
}
