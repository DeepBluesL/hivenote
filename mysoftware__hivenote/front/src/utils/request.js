import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080/zwz', 
  timeout: 10000 // 请求超时时间：10秒
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从浏览器缓存获取 Token
    const token = localStorage.getItem('token')
    if (token) {
      // 在请求头中携带 Token
      config.headers['accessToken'] = token
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：收到后端回复后做的事
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果后端返回的是二进制流(文件下载)，直接返回
    if (response.config.responseType === 'blob') {
        return res
    }

    // 假设后端返回格式: { success: true, message: "...", result: ... }
    // 如果 success 为 false，说明后端报错了
    if (res.success === false) {
      // 如果是未授权错误，清除 token 并跳转登录
      if (res.code === 401 || res.message?.includes('未登录') || res.message?.includes('token')) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      }
      ElMessage.error(res.message || '系统错误')
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res // 请求成功，把数据交出去
    }
  },
  error => {
    console.log('请求错误:', error)
    
    // 处理网络错误
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default service