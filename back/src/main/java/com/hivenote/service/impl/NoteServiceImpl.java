package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.Note;
import com.hivenote.mapper.NoteMapper;
import com.hivenote.service.INoteService;
import org.springframework.stereotype.Service;

/**
 * 笔记业务实现类
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements INoteService {
}
