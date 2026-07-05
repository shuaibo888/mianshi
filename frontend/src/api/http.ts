import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResponse } from '@/types'

export const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('mini_ats_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const payload = response.data as ApiResponse<unknown>
    if (payload.code !== 0) {
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('mini_ats_token')
      router.replace('/login')
      ElMessage.error('登录已失效')
    } else {
      ElMessage.error(error.response?.data?.message || '网络或服务器异常')
    }
    return Promise.reject(error)
  },
)

export async function request<T>(promise: Promise<{ data: ApiResponse<T> }>) {
  const response = await promise
  return response.data.data
}
