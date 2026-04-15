package com.marketback.main.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marketback.main.entity.Report;
import com.marketback.main.mapper.ReportMapper;
import com.marketback.main.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
}
