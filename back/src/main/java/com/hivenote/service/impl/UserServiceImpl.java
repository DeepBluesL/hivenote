package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.User;
import com.hivenote.mapper.UserMapper;
import com.hivenote.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * 必须加上 @Service 注解，Spring 才能扫描到它并注册为 Bean
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // 即使内部为空，也必须继承 ServiceImpl 并实现接口
}