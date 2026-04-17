/**
 * DRP Platform - 日期格式化工具
 *
 * @author Nick
 */

import dayjs, { type Dayjs } from 'dayjs'

// =====================================================
// 格式化常量
// =====================================================

export const DATE_FORMAT = 'YYYY-MM-DD'
export const TIME_FORMAT = 'HH:mm:ss'
export const DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss'
export const TIMESTAMP_FORMAT = 'YYYY-MM-DD HH:mm:ss.SSS'

// =====================================================
// 快捷格式化
// =====================================================

/**
 * 格式化日期
 */
export function formatDate(date: Dayjs | string | Date | number, format: string = DATE_FORMAT): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date: Dayjs | string | Date | number): string {
  return formatDate(date, DATETIME_FORMAT)
}

/**
 * 格式化时间
 */
export function formatTime(date: Dayjs | string | Date | number): string {
  return formatDate(date, TIME_FORMAT)
}

/**
 * 格式化相对时间
 */
export function formatRelativeTime(date: Dayjs | string | Date | number): string {
  if (!date) return ''

  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target)

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 30) return `${days} 天前`
  if (months < 12) return `${months} 个月前`
  return `${years} 年前`
}

/**
 * 格式化时长（毫秒转为可读）
 */
export function formatDuration(ms: number): string {
  if (ms < 0) return '0秒'

  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) {
    return `${days} 天 ${hours % 24} 小时`
  }
  if (hours > 0) {
    return `${hours} 小时 ${minutes % 60} 分钟`
  }
  if (minutes > 0) {
    return `${minutes} 分钟 ${seconds % 60} 秒`
  }
  return `${seconds} 秒`
}

// =====================================================
// 日期计算
// =====================================================

/**
 * 获取今天的开始时间
 */
export function getTodayStart(): Dayjs {
  return dayjs().startOf('day')
}

/**
 * 获取今天的结束时间
 */
export function getTodayEnd(): Dayjs {
  return dayjs().endOf('day')
}

/**
 * 获取指定天数前的时间
 */
export function getDaysAgo(days: number): Dayjs {
  return dayjs().subtract(days, 'day')
}

/**
 * 判断是否是今天
 */
export function isToday(date: Dayjs | string | Date | number): boolean {
  return dayjs(date).isSame(dayjs(), 'day')
}

/**
 * 判断是否是昨天
 */
export function isYesterday(date: Dayjs | string | Date | number): boolean {
  return dayjs(date).isSame(dayjs().subtract(1, 'day'), 'day')
}

// =====================================================
// 导出
// =====================================================

export default {
  DATE_FORMAT,
  TIME_FORMAT,
  DATETIME_FORMAT,
  TIMESTAMP_FORMAT,
  formatDate,
  formatDateTime,
  formatTime,
  formatRelativeTime,
  formatDuration,
  getTodayStart,
  getTodayEnd,
  getDaysAgo,
  isToday,
  isYesterday
}
