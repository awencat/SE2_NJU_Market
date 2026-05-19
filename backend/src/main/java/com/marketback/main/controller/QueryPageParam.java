package com.marketback.main.controller;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class QueryPageParam {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Map<String, Object> param = new HashMap<>();

    public Map<String, Object> getParam() {
        return param;
    }
    public void setParam(Map<String, Object> param) {
        this.param = param;
    }
    public Integer getPageNum() {
        return pageNum;
    }
}
