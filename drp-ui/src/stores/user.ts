/**
 * DRP Platform - 用户状态管理
 *
 * @author Nick
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/api/auth'
import { login as loginApi, getCurrentUser, logout as logoutApi } from '@/api/auth'

// =====================================================
// 类型定义
// =====================================================

/** 登录参数 */
export interface LoginParams {
  username: string
  password: string
}

// =====================================================
// 创建 Store
// =====================================================

export const useUserStore = defineStore('user', () => {
  // =====================================================
  // State
  // =====================================================

  /** 访问令牌 */
  const token = ref(localStorage.getItem('accessToken') || '')

  /** 刷新令牌 */
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')

  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(null)

  // =====================================================
  // Getters
  // =====================================================

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 用户名 */
  const username = computed(() => userInfo.value?.username || '')

  /** 真实姓名 */
  const realName = computed(() => userInfo.value?.realName || '')

  /** 用户角色列表 */
  const roles = computed(() => userInfo.value?.roles || [])

  /** 用户权限列表 */
  const permissions = computed(() => userInfo.value?.permissions || [])

  /** 是否有指定权限 */
  const hasPermission = computed(() => (permission: string) => {
    return permissions.value.includes(permission)
  })

  /** 是否有指定角色 */
  const hasRole = computed(() => (role: string) => {
    return roles.value.includes(role)
  })

  // =====================================================
  // Actions
  // =====================================================

  /**
   * 用户登录
   */
  async function login(params: LoginParams) {
    try {
      const res = await loginApi(params)

      // 保存 Token
      setToken(res.accessToken, res.refreshToken)

      // 保存用户信息
      setUserInfo(res.user)

      return res
    } catch (error) {
      throw error
    }
  }

  /**
   * 获取当前用户信息
   */
  async function fetchUserInfo() {
    try {
      const info = await getCurrentUser()
      setUserInfo(info)
      return info
    } catch (error) {
      // 获取失败时清除用户信息
      userInfo.value = null
      throw error
    }
  }

  /**
   * 用户登出
   */
  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 忽略退出失败
    } finally {
      clearAuth()
    }
  }

  /**
   * 设置访问令牌
   */
  function setToken(accessToken: string, refresh: string = '') {
    token.value = accessToken
    refreshToken.value = refresh
    localStorage.setItem('accessToken', accessToken)
    if (refresh) {
      localStorage.setItem('refreshToken', refresh)
    }
  }

  /**
   * 设置用户信息
   */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  /**
   * 清除认证信息
   */
  function clearAuth() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  // =====================================================
  // 返回
  // =====================================================

  return {
    // State
    token,
    refreshToken,
    userInfo,
    // Getters
    isLoggedIn,
    username,
    realName,
    roles,
    permissions,
    hasPermission,
    hasRole,
    // Actions
    login,
    fetchUserInfo,
    logout,
    setToken,
    setUserInfo,
    clearAuth
  }
})
