/**
 * DRP Platform - 认证模块 API
 *
 * @author Nick
 */

import { get, post } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  roles: string[]
  permissions: string[]
}

/** 登录响应数据 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfo
}

/** 刷新 Token 响应 */
export interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

// =====================================================
// API 方法
// =====================================================

/**
 * 用户登录
 * @param data 登录参数
 */
export function login(data: LoginParams) {
  return post<LoginResponse>('/auth/login', data)
}

/**
 * 刷新 Token
 * @param data 包含 refreshToken 的对象
 */
export function refreshToken(data: { refreshToken: string }) {
  return post<RefreshTokenResponse>('/auth/refresh', data)
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return get<UserInfo>('/auth/userinfo')
}

/**
 * 用户登出
 */
export function logout() {
  return post('/auth/logout')
}
