/**
 * DRP Platform - 通知模块 API
 *
 * @author Nick
 */

import { get, post, put, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 通知渠道 */
export enum NotifyChannel {
  WECOM = 'WECOM',
  DINGTALK = 'DINGTALK',
  FEISHU = 'FEISHU',
  EMAIL = 'EMAIL'
}

/** 通知渠道映射 */
export const NotifyChannelMap: Record<string, { label: string; icon: string; color: string }> = {
  WECOM: { label: '企业微信', icon: 'fab fa-weixin', color: '#07c160' },
  DINGTALK: { label: '钉钉', icon: 'fas fa-bullhorn', color: '#1677ff' },
  FEISHU: { label: '飞书', icon: 'fas fa-egg', color: '#0066cc' },
  EMAIL: { label: '邮件', icon: 'fas fa-envelope', color: '#67c23a' }
}

/** 通知事件 */
export enum NotifyEvent {
  BUILD_START = 'BUILD_START',
  BUILD_SUCCESS = 'BUILD_SUCCESS',
  BUILD_FAIL = 'BUILD_FAIL',
  DEPLOY_START = 'DEPLOY_START',
  DEPLOY_SUCCESS = 'DEPLOY_SUCCESS',
  DEPLOY_FAIL = 'DEPLOY_FAIL',
  DEPLOY_APPROVAL = 'DEPLOY_APPROVAL',
  ROLLBACK_SUCCESS = 'ROLLBACK_SUCCESS',
  ROLLBACK_FAIL = 'ROLLBACK_FAIL',
  SERVER_OFFLINE = 'SERVER_OFFLINE',
  SERVER_ALERT = 'SERVER_ALERT',
  LOGIN_FAILED = 'LOGIN_FAILED',
  PERMISSION_DENIED = 'PERMISSION_DENIED'
}

/** 通知事件映射 */
export const NotifyEventMap: Record<string, { label: string; category: string; severity: string }> = {
  BUILD_START: { label: '构建开始', category: 'BUILD', severity: 'INFO' },
  BUILD_SUCCESS: { label: '构建成功', category: 'BUILD', severity: 'INFO' },
  BUILD_FAIL: { label: '构建失败', category: 'BUILD', severity: 'WARNING' },
  DEPLOY_START: { label: '部署开始', category: 'DEPLOY', severity: 'INFO' },
  DEPLOY_SUCCESS: { label: '部署成功', category: 'DEPLOY', severity: 'INFO' },
  DEPLOY_FAIL: { label: '部署失败', category: 'DEPLOY', severity: 'WARNING' },
  DEPLOY_APPROVAL: { label: '待审批', category: 'DEPLOY', severity: 'INFO' },
  ROLLBACK_SUCCESS: { label: '回滚成功', category: 'ROLLBACK', severity: 'WARNING' },
  ROLLBACK_FAIL: { label: '回滚失败', category: 'ROLLBACK', severity: 'CRITICAL' },
  SERVER_OFFLINE: { label: '服务器离线', category: 'SERVER', severity: 'CRITICAL' },
  SERVER_ALERT: { label: '服务器告警', category: 'SERVER', severity: 'WARNING' },
  LOGIN_FAILED: { label: '登录失败', category: 'SECURITY', severity: 'WARNING' },
  PERMISSION_DENIED: { label: '权限异常', category: 'SECURITY', severity: 'WARNING' }
}

/** 通知状态 */
export enum NotifyStatus {
  PENDING = 'PENDING',
  SUCCESS = 'SUCCESS',
  FAIL = 'FAIL',
  RETRY = 'RETRY'
}

/** 通知状态映射 */
export const NotifyStatusMap: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待发送', type: 'info' },
  SUCCESS: { label: '成功', type: 'success' },
  FAIL: { label: '失败', type: 'danger' },
  RETRY: { label: '重试中', type: 'warning' }
}

/** 通知配置 */
export interface NotificationConfig {
  id?: number
  projectId?: number
  projectName?: string
  channel: string
  event: string
  enabled: boolean
  config: Record<string, any>
  recipients: string
  templateId?: number
  rateLimit?: number
  createTime?: string
}

/** 通知记录 */
export interface NotificationRecord {
  id: number
  configId: number
  channel: string
  event: string
  recipient: string
  title: string
  content: string
  status: string
  statusText: string
  retryCount: number
  maxRetry: number
  errorMessage?: string
  sendTime: string
  sentTime?: string
  externalId?: string
}

/** 通知模板 */
export interface NotificationTemplate {
  id?: number
  name: string
  channel: string
  event: string
  titleTemplate: string
  contentTemplate: string
  variables?: string
  enabled: boolean
}

/** 渠道信息 */
export interface ChannelInfo {
  code: string
  name: string
  configSchema: string
}

// =====================================================
// API 方法
// =====================================================

/**
 * 获取通知配置列表
 */
export function getNotifyConfigs(params?: {
  projectId?: number
  channel?: string
  page?: number
  pageSize?: number
}) {
  return get<{ records: NotificationConfig[]; total: number; page: number; size: number }>('/notify/configs', params)
}

/**
 * 获取通知配置详情
 */
export function getNotifyConfig(id: number) {
  return get<NotificationConfig>(`/notify/configs/${id}`)
}

/**
 * 创建通知配置
 */
export function createNotifyConfig(data: Partial<NotificationConfig>) {
  return post<null>('/notify/configs', data)
}

/**
 * 更新通知配置
 */
export function updateNotifyConfig(id: number, data: Partial<NotificationConfig>) {
  return put<null>(`/notify/configs/${id}`, data)
}

/**
 * 删除通知配置
 */
export function deleteNotifyConfig(id: number) {
  return del<null>(`/notify/configs/${id}`)
}

/**
 * 测试渠道连通性
 */
export function testNotifyChannel(data: { channel: string; config: Record<string, any> }) {
  return post<boolean>('/notify/test', data)
}

/**
 * 获取支持的渠道列表
 */
export function getNotifyChannels() {
  return get<ChannelInfo[]>('/notify/channels')
}

/**
 * 获取通知记录列表
 */
export function getNotifyRecords(params?: {
  channel?: string
  status?: string
  page?: number
  pageSize?: number
}) {
  return get<{ records: NotificationRecord[]; total: number; page: number; size: number }>('/notify/records', params)
}

/**
 * 获取通知记录详情
 */
export function getNotifyRecord(id: number) {
  return get<NotificationRecord>(`/notify/records/${id}`)
}

/**
 * 重试发送通知
 */
export function retryNotifyRecord(id: number) {
  return post<null>(`/notify/records/${id}/retry`)
}

/**
 * 获取通知模板列表
 */
export function getNotifyTemplates(params?: {
  channel?: string
  event?: string
  page?: number
  pageSize?: number
}) {
  return get<{ records: NotificationTemplate[]; total: number; page: number; size: number }>('/notify/templates', params)
}

/**
 * 获取通知模板详情
 */
export function getNotifyTemplate(id: number) {
  return get<NotificationTemplate>(`/notify/templates/${id}`)
}

/**
 * 创建通知模板
 */
export function createNotifyTemplate(data: Partial<NotificationTemplate>) {
  return post<null>('/notify/templates', data)
}

/**
 * 更新通知模板
 */
export function updateNotifyTemplate(id: number, data: Partial<NotificationTemplate>) {
  return put<null>(`/notify/templates/${id}`, data)
}

/**
 * 删除通知模板
 */
export function deleteNotifyTemplate(id: number) {
  return del<null>(`/notify/templates/${id}`)
}
