package com.hivenote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hivenote.entity.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记数据访问层
 * 对应表: t_note
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    
    // 举个例子：如果你将来想统计每个分类下的笔记数量
    // @Select("SELECT category, COUNT(*) as count FROM t_note GROUP BY category")
    // List<Map<String, Object>> countByCategory();
}
