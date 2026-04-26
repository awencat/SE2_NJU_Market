package com.marketback.main.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.marketback.main.common.ApiResponse;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public abstract class BaseCrudController<T> {

    private final IService<T> service;
    private final String idFieldName;

    protected BaseCrudController(IService<T> service, String idFieldName) {
        this.service = service;
        this.idFieldName = idFieldName;
    }

    @GetMapping
    public ApiResponse<?> list(
            T query,
            @RequestParam(value = "current", required = false) Long current,
            @RequestParam(value = "size", required = false) Long size) {
        QueryWrapper<T> wrapper = new QueryWrapper<>(query);
        if (current != null && size != null) {
            IPage<T> page = service.page(new Page<>(current, size), wrapper);
            return ApiResponse.success(page);
        }
        List<T> list = service.list(wrapper);
        return ApiResponse.success(list);
    }

    @PostMapping("/search")
    public ApiResponse<?> search(@RequestBody(required = false) Map<String, Object> filters) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (value != null && !"".equals(value)) {
                    wrapper.eq(toSnakeCase(key), value);
                }
            });
        }
        return ApiResponse.success(service.list(wrapper));
    }

    @GetMapping("/{id}")
    public ApiResponse<T> getById(@PathVariable Integer id) {
        T entity = service.getById(id);
        if (entity == null) {
            return ApiResponse.fail("record not found");
        }
        return ApiResponse.success(entity);
    }

    @PostMapping
    public ApiResponse<T> create(@RequestBody T entity) {
        boolean saved = service.save(entity);
        if (!saved) {
            return ApiResponse.fail("create failed");
        }
        return ApiResponse.success("created", entity);
    }

    @PutMapping("/{id}")
    public ApiResponse<T> update(@PathVariable Integer id, @RequestBody T entity) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(entity);
        wrapper.setPropertyValue(idFieldName, id);
        boolean updated = service.updateById(entity);
        if (!updated) {
            return ApiResponse.fail("update failed");
        }
        return ApiResponse.success("updated", entity);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        boolean removed = service.removeById(id);
        if (!removed) {
            return ApiResponse.fail("delete failed");
        }
        return ApiResponse.success("deleted", true);
    }

    private String toSnakeCase(String value) {
        return value.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
