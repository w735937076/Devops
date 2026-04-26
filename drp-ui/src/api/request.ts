/**
 * DRP Platform - Axios HTTP 请求封装
 *
 * 功能特性：
 * - 统一拦截请求/响应
 * - Token 自动附加
 * - Token 过期自动刷新
 * - 统一错误处理
 * - 请求取消
 *
 * @author Nick
 */

import axios, { AxiosHeaders, AxiosInstance, AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// =====================================================
// 类型定义
// =====================================================

/** API 响应数据结构 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/** 请求配置扩展 */
export interface RequestConfig extends AxiosRequestConfig {
  /** 是否忽略 Token */
  ignoreToken?: boolean
  /** 是否显示错误提示 */
  showError?: boolean
}

/** 分页响应 */
export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// =====================================================
// 创建 Axios 实例
// =====================================================

const service: AxiosInstance = axios.create({
  // API 基础路径
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  // 请求超时时间
  timeout: 30000,
  // 默认 Content-Type
  headers: {
    'Content-Type': 'application/json'
  }
})

// =====================================================
// 请求拦截器
// =====================================================

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从本地存储获取 Token
    const accessToken = localStorage.getItem('accessToken')
    if (accessToken && !config.headers.get('Authorization')) {
      config.headers.set('Authorization', `Bearer ${accessToken}`)
    }

    return config
  },
  (error: AxiosError) => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

// =====================================================
// 响应拦截器
// =====================================================

// 标记是否正在刷新 Token，避免多个请求同时刷新
let isRefreshing = false
// 等待 Token 刷新成功的请求队列
let refreshSubscribers: ((token: string) => void)[] = []

/**
 * 将请求添加到等待队列
 */
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

/**
 * 刷新成功后，执行队列中的请求
 */
function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach((callback) => callback(token))
  refreshSubscribers = []
}

function ensureHeaders(config: RequestConfig) {
  if (!config.headers) {
    config.headers = new AxiosHeaders()
  } else if (!(config.headers instanceof AxiosHeaders)) {
    config.headers = new AxiosHeaders(config.headers as any)
  }
  return config.headers
}

/**
 * 处理 Token 过期
 */
async function handleTokenExpired(originalConfig: RequestConfig): Promise<any> {
  if (!isRefreshing) {
    isRefreshing = true

    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        throw new Error('No refresh token')
      }

      // 调用刷新接口
      const res = await axios.post('/api/auth/refresh', { refreshToken })
      // 后端返回格式: { code, message, data: { accessToken, refreshToken, ... } }
      const { accessToken, refreshToken: newRefreshToken } = res.data.data

      // 更新本地存储
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', newRefreshToken)

      // 执行等待队列
      onTokenRefreshed(accessToken)
      isRefreshing = false

      // 重试原请求
      ensureHeaders(originalConfig).set('Authorization', `Bearer ${accessToken}`)
      return service(originalConfig)
    } catch (error) {
      isRefreshing = false

      // 刷新失败，清除存储并跳转登录
      localStorage.clear()
      router.push('/login')

      return Promise.reject(error)
    }
  } else {
    // 正在刷新，返回一个 Promise，等待刷新成功后执行
    return new Promise((resolve) => {
      subscribeTokenRefresh((token: string) => {
        ensureHeaders(originalConfig).set('Authorization', `Bearer ${token}`)
        resolve(service(originalConfig))
      })
    })
  }
}

// =====================================================
// 响应拦截器处理
// =====================================================

service.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data as ApiResponse

    // 根据业务状态码判断
    if (code === 200 || code === 0) {
      return data
    }

    // 其他业务错误
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  async (error: AxiosError<ApiResponse>) => {
    const { response, config } = error
    const originalConfig = config as RequestConfig

    // 默认不显示错误提示
    const showError = originalConfig.showError !== false

    // HTTP 状态码处理
    if (response) {
      switch (response.status) {
        case 401:
          // Token 过期，尝试刷新
          try {
            await handleTokenExpired(originalConfig)
          } catch {
            if (showError) {
              ElMessage.error('登录已过期，请重新登录')
            }
          }
          break

        case 403:
          if (showError) {
            ElMessage.error('无权限访问')
          }
          break

        case 404:
          if (showError) {
            ElMessage.error('请求的资源不存在')
          }
          break

        case 500:
          if (showError) {
            ElMessage.error('服务器错误，请稍后重试')
          }
          break

        default:
          if (showError) {
            const msg = response.data?.message || error.message || '网络异常'
            ElMessage.error(msg)
          }
      }
    } else {
      // 网络错误
      if (showError) {
        ElMessage.error('网络连接失败，请检查网络')
      }
    }

    return Promise.reject(error)
  }
)

// =====================================================
// 导出请求方法
// =====================================================

export default service

/**
 * GET 请求
 */
export function get<T = any>(url: string, params?: any, config?: RequestConfig): Promise<T> {
  return service.get(url, { params, ...config })
}

/**
 * POST 请求
 */
export function post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
  return service.post(url, data, config)
}

/**
 * PUT 请求
 */
export function put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
  return service.put(url, data, config)
}

/**
 * DELETE 请求
 */
export function del<T = any>(url: string, params?: any, config?: RequestConfig): Promise<T> {
  return service.delete(url, { params, ...config })
}

/**
 * PATCH 请求
 */
export function patch<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
  return service.patch(url, data, config)
}
