package com.hivenote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hivenote.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 * 对应表: t_user
 * 继承 BaseMapper 后，自动拥有 CRUD 能力
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 如果后续有极其复杂的 SQL (比如多表关联统计)，可以在这里定义方法
    // 并在 src/main/resources/mapper/UserMapper.xml 中写 SQL
    // 但对于目前的需求，BaseMapper 已经足够了！
}