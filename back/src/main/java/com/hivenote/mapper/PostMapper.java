package com.hivenote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hivenote.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
