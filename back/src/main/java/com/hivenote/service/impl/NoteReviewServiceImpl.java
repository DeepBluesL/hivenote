package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.NoteReview;
import com.hivenote.mapper.NoteReviewMapper;
import com.hivenote.service.INoteReviewService;
import org.springframework.stereotype.Service;

@Service
public class NoteReviewServiceImpl extends ServiceImpl<NoteReviewMapper, NoteReview> implements INoteReviewService {
}
