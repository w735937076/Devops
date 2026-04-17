/**
 * DRP Platform - 表单验证工具
 *
 * @author Nick
 */

// =====================================================
// 常用正则表达式
// =====================================================

export const patterns = {
  // 用户名：4-16位字母、数字、下划线
  username: /^[a-zA-Z0-9_]{4,16}$/,
  // 密码：至少8位，包含大小写字母和数字
  password: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,}$/,
  // 手机号
  phone: /^1[3-9]\d{9}$/,
  // 邮箱
  email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  // URL
  url: /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)$/,
  // IP地址
  ip: /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
  // 端口号
  port: /^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$/,
  // Git 仓库 URL
  gitUrl: /^(https?:\/\/.*\.git|ssh:\/\/.*|git@.*:.*)$/,
  // 版本号 (如 v1.0.0)
  version: /^v\d+(\.\d+)*(-\d+)*$/
}

// =====================================================
// 验证函数
// =====================================================

export const validators = {
  /**
   * 必填验证
   */
  required(message = '此字段为必填项') {
    return {
      required: true,
      message,
      trigger: 'blur'
    }
  },

  /**
   * 用户名验证
   */
  username() {
    return {
      pattern: patterns.username,
      message: '用户名由4-16位字母、数字或下划线组成',
      trigger: 'blur'
    }
  },

  /**
   * 密码验证
   */
  password(message = '密码至少8位，包含大小写字母和数字') {
    return {
      pattern: patterns.password,
      message,
      trigger: 'blur'
    }
  },

  /**
   * 手机号验证
   */
  phone(message = '请输入正确的手机号') {
    return {
      pattern: patterns.phone,
      message,
      trigger: 'blur'
    }
  },

  /**
   * 邮箱验证
   */
  email(message = '请输入正确的邮箱地址') {
    return {
      pattern: patterns.email,
      message,
      trigger: 'blur'
    }
  },

  /**
   * URL 验证
   */
  url(message = '请输入正确的 URL') {
    return {
      pattern: patterns.url,
      message,
      trigger: 'blur'
    }
  },

  /**
   * IP 地址验证
   */
  ip(message = '请输入正确的 IP 地址') {
    return {
      pattern: patterns.ip,
      message,
      trigger: 'blur'
    }
  },

  /**
   * 端口号验证
   */
  port(message = '请输入正确的端口号 (0-65535)') {
    return {
      pattern: patterns.port,
      message,
      trigger: 'blur'
    }
  },

  /**
   * 长度验证
   */
  length(min: number, max: number, message?: string) {
    return {
      min,
      max,
      message: message || `长度应在 ${min}-${max} 个字符之间`,
      trigger: 'blur'
    }
  },

  /**
   * 最小长度验证
   */
  minLength(min: number, message?: string) {
    return {
      min,
      message: message || `至少 ${min} 个字符`,
      trigger: 'blur'
    }
  },

  /**
   * 最大长度验证
   */
  maxLength(max: number, message?: string) {
    return {
      max,
      message: message || `最多 ${max} 个字符`,
      trigger: 'blur'
    }
  },

  /**
   * 自定义正则验证
   */
  pattern(reg: RegExp, message: string) {
    return {
      pattern: reg,
      message,
      trigger: 'blur'
    }
  },

  /**
   * 远程验证 (预留)
   */
  remote(validator: (value: any) => Promise<boolean>, message: string) {
    return {
      validator: async (_rule: any, value: any, callback: any) => {
        try {
          const isValid = await validator(value)
          if (isValid) {
            callback()
          } else {
            callback(new Error(message))
          }
        } catch {
          callback(new Error('验证失败'))
        }
      },
      trigger: 'blur'
    }
  }
}

// =====================================================
// 导出
// =====================================================

export default {
  patterns,
  validators
}
