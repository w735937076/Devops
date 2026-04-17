/**
 * DRP Platform - 颜色工具
 *
 * @author Nick
 */

// =====================================================
// 颜色格式转换
// =====================================================

/**
 * HEX 转 RGB
 */
export function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
      }
    : null
}

/**
 * RGB 转 HEX
 */
export function rgbToHex(r: number, g: number, b: number): string {
  return '#' + [r, g, b].map((x) => x.toString(16).padStart(2, '0')).join('')
}

/**
 * RGBA 转 HEX (带透明度)
 */
export function rgbaToHex(r: number, g: number, b: number, a: number): string {
  const alpha = Math.round(a * 255)
    .toString(16)
    .padStart(2, '0')
  return rgbToHex(r, g, b) + alpha
}

// =====================================================
// 颜色操作
// =====================================================

/**
 * 颜色加深
 */
export function darken(color: string, percent: number): string {
  const rgb = hexToRgb(color)
  if (!rgb) return color

  const factor = 1 - percent / 100
  return rgbToHex(
    Math.round(rgb.r * factor),
    Math.round(rgb.g * factor),
    Math.round(rgb.b * factor)
  )
}

/**
 * 颜色变浅
 */
export function lighten(color: string, percent: number): string {
  const rgb = hexToRgb(color)
  if (!rgb) return color

  const factor = percent / 100
  return rgbToHex(
    Math.round(rgb.r + (255 - rgb.r) * factor),
    Math.round(rgb.g + (255 - rgb.g) * factor),
    Math.round(rgb.b + (255 - rgb.b) * factor)
  )
}

/**
 * 设置颜色透明度
 */
export function setAlpha(color: string, alpha: number): string {
  const rgb = hexToRgb(color)
  if (!rgb) return color
  return `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${alpha})`
}

// =====================================================
// 颜色判断
// =====================================================

/**
 * 判断颜色是否为深色
 */
export function isDark(color: string): boolean {
  const rgb = hexToRgb(color)
  if (!rgb) return false

  // 计算相对亮度
  const luminance = (0.299 * rgb.r + 0.587 * rgb.g + 0.114 * rgb.b) / 255
  return luminance < 0.5
}

/**
 * 获取对比色（黑色或白色）
 */
export function getContrastColor(color: string): string {
  return isDark(color) ? '#ffffff' : '#000000'
}

// =====================================================
// 预设颜色
// =====================================================

/**
 * 状态颜色映射
 */
export const statusColors = {
  // 成功/完成
  success: '#67c23a',
  // 警告
  warning: '#e6a23c',
  // 危险/错误
  danger: '#f56c6c',
  // 信息
  info: '#909399',
  // 进行中
  primary: '#409eff',
  // 禁用
  disabled: '#c0c4cc'
}

/**
 * 根据状态获取颜色
 */
export function getStatusColor(status: string | number): string {
  const statusMap: Record<string, string> = {
    // 通用状态
    0: statusColors.disabled,
    1: statusColors.primary,
    2: statusColors.success,
    3: statusColors.danger,
    4: statusColors.warning,
    // 通用
    success: statusColors.success,
    warning: statusColors.warning,
    error: statusColors.danger,
    info: statusColors.info,
    primary: statusColors.primary,
    disabled: statusColors.disabled,
    // 构建状态
    pending: statusColors.info,
    running: statusColors.primary,
    failed: statusColors.danger,
    cancelled: statusColors.disabled,
    // 部署状态
    deployed: statusColors.success,
    deploying: statusColors.primary,
    rollback: statusColors.warning,
    offline: statusColors.disabled
  }
  return statusMap[String(status).toLowerCase()] || statusColors.info
}

// =====================================================
// 导出
// =====================================================

export default {
  hexToRgb,
  rgbToHex,
  rgbaToHex,
  darken,
  lighten,
  setAlpha,
  isDark,
  getContrastColor,
  statusColors,
  getStatusColor
}
