package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.Comment;
import com.hivenote.mapper.CommentMapper;
import com.hivenote.service.ICommentService;
import org.springframework.stereotype.Service;

/**
 * 评论业务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
}
