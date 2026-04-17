/**
 * DRP Platform - 本地存储工具
 *
 * @author Nick
 */

// =====================================================
// 存储键名枚举
// =====================================================

export const StorageKeys = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER_INFO: 'userInfo',
  SIDEBAR_COLLAPSED: 'sidebarCollapsed',
  THEME: 'theme',
  LANGUAGE: 'language'
} as const

// =====================================================
// 类型定义
// =====================================================

type ValueType = string | number | boolean | object | null

// =====================================================
// 存储工具类
// =====================================================

class StorageUtil {
  private prefix = 'drp_'

  /**
   * 设置值
   */
  set(key: string, value: ValueType): void {
    try {
      const fullKey = this.prefix + key
      if (value === null || value === undefined) {
        localStorage.removeItem(fullKey)
        return
      }

      const serialized = typeof value === 'string' ? value : JSON.stringify(value)
      localStorage.setItem(fullKey, serialized)
    } catch (error) {
      console.error('Storage set error:', error)
    }
  }

  /**
   * 获取值
   */
  get<T = string>(key: string, defaultValue: T | null = null): T | null {
    try {
      const fullKey = this.prefix + key
      const value = localStorage.getItem(fullKey)

      if (value === null) {
        return defaultValue
      }

      // 尝试解析为 JSON
      try {
        return JSON.parse(value) as T
      } catch {
        return value as unknown as T
      }
    } catch (error) {
      console.error('Storage get error:', error)
      return defaultValue
    }
  }

  /**
   * 移除值
   */
  remove(key: string): void {
    const fullKey = this.prefix + key
    localStorage.removeItem(fullKey)
  }

  /**
   * 清空所有值
   */
  clear(): void {
    const keys = Object.values(StorageKeys).map((k) => this.prefix + k)
    keys.forEach((key) => localStorage.removeItem(key))
  }

  /**
   * 检查是否存在
   */
  has(key: string): boolean {
    const fullKey = this.prefix + key
    return localStorage.getItem(fullKey) !== null
  }
}

// =====================================================
// 导出单例
// =====================================================

export const storage = new StorageUtil()

// 便捷方法
export const setToken = (token: string) => storage.set(StorageKeys.ACCESS_TOKEN, token)
export const getToken = () => storage.get<string>(StorageKeys.ACCESS_TOKEN)
export const removeToken = () => storage.remove(StorageKeys.ACCESS_TOKEN)

export const setRefreshToken = (token: string) => storage.set(StorageKeys.REFRESH_TOKEN, token)
export const getRefreshToken = () => storage.get<string>(StorageKeys.REFRESH_TOKEN)
export const removeRefreshToken = () => storage.remove(StorageKeys.REFRESH_TOKEN)
