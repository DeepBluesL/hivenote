package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.ContentReport;
import com.hivenote.mapper.ContentReportMapper;
import com.hivenote.service.IContentReportService;
import org.springframework.stereotype.Service;

@Service
public class ContentReportServiceImpl extends ServiceImpl<ContentReportMapper, ContentReport> implements IContentReportService {
}
