package com.hivenote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hivenote.entity.User;

/**
 * 用户业务接口
 * 继承 IService<User> 后，自动拥有了对 User 表的所有基础业务方法
 */
public interface IUserService extends IService<User> {
    // 如果以后有复杂的登录逻辑（比如集成微信登录），可以在这里定义方法
}