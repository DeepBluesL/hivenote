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

/**
 * 管理员获取用户列表
 */
export const getAdminUserList = (params) => {
  return request({
    url: '/user/admin/list',
    method: 'get',
    params,
    silent: true
  })
}

/**
 * 管理员封禁用户
 */
export const banUser = (id) => {
  return request({
    url: `/user/admin/ban/${id}`,
    method: 'post'
  })
}

/**
 * 管理员解封用户
 */
export const unbanUser = (id) => {
  return request({
    url: `/user/admin/unban/${id}`,
    method: 'post'
  })
}

/**
 * 上传头像图片
 */
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/uploadAvatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}

/**
 * 设置用户头像URL
 */
export const setAvatar = (avatarUrl) => {
  return request({
    url: '/user/uploadAvatar',
    method: 'post',
    data: { avatarUrl }
  })
}

/**
 * 管理员封禁用户头像
 */
export const banAvatar = (id) => {
  return request({
    url: `/user/admin/banAvatar/${id}`,
    method: 'post'
  })
}

/**
 * 获取通知列表
 */
export const getNotifications = () => {
  return request({
    url: '/user/notifications',
    method: 'get',
    silent: true
  })
}

/**
 * 标记通知已读
 */
export const markNotificationRead = (id) => {
  return request({
    url: `/user/notifications/read/${id}`,
    method: 'post',
    silent: true
  })
}

/**
 * 全部标记已读
 */
export const markAllNotificationsRead = () => {
  return request({
    url: '/user/notifications/readAll',
    method: 'post',
    silent: true
  })
}

/**
 * 获取未读通知数量
 */
export const getUnreadNotificationCount = () => {
  return request({
    url: '/user/notifications/unreadCount',
    method: 'get',
    silent: true
  })
}