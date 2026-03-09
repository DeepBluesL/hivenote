const TokenKey = 'token'
const UserInfoKey = 'userInfo'

// 获取token
export function getToken() {
  return localStorage.getItem(TokenKey)
}

// 设置token
export function setToken(token) {
  return localStorage.setItem(TokenKey, token)
}

// 移除token
export function removeToken() {
  return localStorage.removeItem(TokenKey)
}

// 获取用户信息
export function getUserInfo() {
  const userInfo = localStorage.getItem(UserInfoKey)
  return userInfo ? JSON.parse(userInfo) : null
}

// 设置用户信息
export function setUserInfo(userInfo) {
  return localStorage.setItem(UserInfoKey, JSON.stringify(userInfo))
}

// 移除用户信息
export function removeUserInfo() {
  return localStorage.removeItem(UserInfoKey)
}

// 清除所有认证信息（登出）
export function clearAuth() {
  removeToken()
  removeUserInfo()
  // 可选：清除其他相关数据
  localStorage.removeItem('userRole')
  localStorage.removeItem('permissions')
}

// 检查是否已登录
export function isLoggedIn() {
  return !!getToken()
}

// 退出登录（兼容旧代码）
export function logout() {
  clearAuth()
}

// 检查是否是管理员
export function isAdmin() {
  const userInfo = getUserInfo()
  return userInfo && userInfo.type === 1
}

// 获取用户类型
export function getUserType() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.type : null
}

// 获取用户ID
export function getUserId() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.id : null
}

// 获取用户名
export function getUsername() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.username : null
}

// 获取用户昵称
export function getNickname() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.nickname : null
}
