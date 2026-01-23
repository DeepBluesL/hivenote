import request from '../utils/request'

/**
 * 登录接口
 * @param {Object} data - { username: '', password: '' }
 */
export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data // 使用 data 传递 JSON 格式数据
  })
}

/**
 * 注册接口
 * @param {Object} data - { username: '', password: '', nickname: '', email: '' }
 */
export const register = (data) => {
  return request({
    url: '/user/regist', // 对应后端 UserController 的注册接口
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息
 * (通常用于进入首页后，获取用户头像、角色等)
 */
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 修改个人资料
 * @param {Object} data 
 */
export const updateUserProfile = (data) => {
  return request({
    url: '/user/edit',
    method: 'post',
    data
  })
}

/**
 * 退出登录 (如果后端有专门的注销接口)
 */
export const logout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword: '', newPassword: '' }
 */
export const changePassword = (data) => {
  return request({
    url: '/user/changePassword',
    method: 'post',
    data
  })
}

/**
 * 管理员登录接口
 * @param {Object} data - { username: '', password: '' }
 */
export const adminLogin = (data) => {
  return request({
    url: '/user/adminLogin',
    method: 'post',
    data
  })
}