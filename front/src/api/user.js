import request from '@/utils/request'

// 普通用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 管理员登录
export function adminLogin(data) {
  console.log('调用管理员登录API:', data)
  return request({
    url: '/user/adminLogin',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/user/regist',
    method: 'post',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/changePassword',
    method: 'post',
    data
  })
}

// 修改个人资料 - 对应后端 /user/edit 接口
export function updateProfile(data) {
  return request({
    url: '/user/edit',
    method: 'post',
    data
  })
}

// 别名，兼容 Profile.vue 中的导入
export const updateUserProfile = updateProfile

// 获取我的笔记列表
export function getMyNotes() {
  return request({
    url: '/note/myList',
    method: 'get'
  })
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
