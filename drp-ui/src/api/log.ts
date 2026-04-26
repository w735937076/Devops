/**
 * DRP Platform - 日志管理模块 API
 *
 * @author Nick
 */

import { get } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 操作类型 */
export enum OperationType {
  BUILD = 'BUILD',
  DEPLOY = 'DEPLOY',
  ROLLBACK = 'ROLLBACK',
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE'
}

/** 操作类型映射 */
export const OperationTypeMap: Record<string, { label: string; type: string }> = {
  [OperationType.BUILD]: { label: '构建', type: 'success' },
  [OperationType.DEPLOY]: { label: '部署', type: 'primary' },
  [OperationType.ROLLBACK]: { label: '回滚', type: 'warning' },
  [OperationType.CREATE]: { label: '创建', type: 'info' },
  [OperationType.UPDATE]: { label: '更新', type: 'primary' },
  [OperationType.DELETE]: { label: '删除', type: 'danger' }
}

/** 操作日志状态 */
export enum LogStatus {
  SUCCESS = 'SUCCESS',
  FAIL = 'FAIL'
}

/** 操作日志 */
export interface OperationLog {
  id: number
  operator: string
  operatorId: number
  operationType: string
  operationTypeText: string
  projectId: number
  projectName: string
  env: string
  version: string
  detail: string
  ip: string
  status: LogStatus
  statusText: string
  duration: number
  errorMessage: string
  createTime: string
}

/** 日志查询参数 */
export interface LogQuery {
  operatorId?: number
  projectId?: number
  operationType?: string
  startTime?: string
  endTime?: string
  keyword?: string
  page?: number
  size?: number
  dateRange?: string[]
}

/** 构建日志响应 */
export interface BuildLogResponse {
  log: string
  lineCount: number
}

/** 服务器状态 */
export interface ServerStatus {
  id: number
  name: string
  ip: string
  hostname?: string
  app: string
  online: boolean
}

// =====================================================
// API 方法
// =====================================================

/**
 * 获取操作日志列表
 */
export function getOperationLogs(params?: LogQuery) {
  return get<{ records: OperationLog[]; total: number; page: number; size: number }>('/logs/operations', params)
}

/**
 * 获取操作日志详情
 */
export function getOperationLogDetail(id: number) {
  return get<OperationLog>(`/logs/operations/${id}`)
}

/**
 * 导出操作日志
 */
export function exportOperationLogs(params?: LogQuery) {
  const queryString = new URLSearchParams()
  if (params?.operatorId) queryString.append('operatorId', String(params.operatorId))
  if (params?.projectId) queryString.append('projectId', String(params.projectId))
  if (params?.operationType) queryString.append('operationType', params.operationType)
  if (params?.startTime) queryString.append('startTime', params.startTime)
  if (params?.endTime) queryString.append('endTime', params.endTime)
  if (params?.keyword) queryString.append('keyword', params.keyword)

  window.open(`/api/logs/operations/export?${queryString.toString()}`)
}

/**
 * 获取构建日志
 */
export function getBuildLogs(buildId: number, lineFrom?: number, lineLimit: number = 1000) {
  const params: Record<string, any> = { lineLimit }
  if (lineFrom) params.lineFrom = lineFrom
  return get<string>(`/logs/builds/${buildId}`, params)
}

/**
 * 获取构建日志流状态
 */
export function isBuildLogStreamActive(buildId: number) {
  return get<boolean>(`/logs/builds/${buildId}/stream`)
}

// WebSocket URL 工厂函数
export function getAppLogWsUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  return `${protocol}//${host}/ws/log/server`
}
