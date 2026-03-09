import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from './auth'

// 创建axios实例
const service = axios.create({
  baseURL: 'http://localhost:8080/zwz', // 后端接口地址
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = getToken()
    if (token) {
      config.headers['accessToken'] = token
    }
    console.log('发送请求:', config.url, config.data)
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    console.log('收到响应:', res)
    
    // 根据后端返回格式处理
    if (res.code === 200 || res.success === true) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('响应错误:', error)
    ElMessage.error(error.response?.data?.message || '网络错误，请检查后端是否启动')
    return Promise.reject(error)
  }
)

export default service
