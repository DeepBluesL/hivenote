package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.PostLike;
import com.hivenote.mapper.PostLikeMapper;
import com.hivenote.service.IPostLikeService;
import org.springframework.stereotype.Service;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements IPostLikeService {
}
