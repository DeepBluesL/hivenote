package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.PostReply;
import com.hivenote.mapper.PostReplyMapper;
import com.hivenote.service.IPostReplyService;
import org.springframework.stereotype.Service;

@Service
public class PostReplyServiceImpl extends ServiceImpl<PostReplyMapper, PostReply> implements IPostReplyService {
}
