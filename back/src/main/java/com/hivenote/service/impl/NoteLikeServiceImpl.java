package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.NoteLike;
import com.hivenote.mapper.NoteLikeMapper;
import com.hivenote.service.INoteLikeService;
import org.springframework.stereotype.Service;

@Service
public class NoteLikeServiceImpl extends ServiceImpl<NoteLikeMapper, NoteLike> implements INoteLikeService {
}
