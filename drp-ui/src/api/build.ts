/**
 * DRP Platform - 构建管理模块 API
 *
 * @author Nick
 */

import { get, post, put, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 构建状态枚举 */
export enum BuildStatus {
  PENDING = 0,
  RUNNING = 1,
  SUCCESS = 2,
  FAILED = 3,
  CANCELLED = 4,
  TIMEOUT = 5
}

/** 构建状态映射 */
export const BuildStatusMap: Record<number, { label: string; type: string }> = {
  [BuildStatus.PENDING]: { label: '排队中', type: 'info' },
  [BuildStatus.RUNNING]: { label: '运行中', type: 'primary' },
  [BuildStatus.SUCCESS]: { label: '成功', type: 'success' },
  [BuildStatus.FAILED]: { label: '失败', type: 'danger' },
  [BuildStatus.CANCELLED]: { label: '已取消', type: 'warning' },
  [BuildStatus.TIMEOUT]: { label: '超时', type: 'danger' }
}

/** 触发类型 */
export enum TriggerType {
  MANUAL = 'MANUAL',
  PUSH = 'PUSH',
  TAG = 'TAG',
  SCHEDULE = 'SCHEDULE',
  API = 'API'
}

/** 构建记录 */
export interface Build {
  id: number
  buildNumber: number
  projectId: number
  projectName: string
  pipelineId: number
  pipelineName: string
  branch: string
  commitId: string
  commitMessage: string
  status: BuildStatus
  statusDesc: string
  triggerType: string
  triggerUser: string
  duration: number
  startTime: string
  endTime: string
  errorMessage: string
  artifacts: Artifact[]
  createTime: string
}

/** 构建产物 */
export interface Artifact {
  name: string
  path: string
  size: number
  downloadUrl: string
}

/** 构建查询参数 */
export interface BuildQuery {
  projectId?: number
  status?: BuildStatus
  branch?: string
  page?: number
  pageSize?: number
}

/** 触发构建请求 */
export interface BuildTriggerRequest {
  projectId: number
  branch: string
  commitId?: string
  pipelineId?: number
  triggerType?: TriggerType
}

/** 流水线配置 */
export interface Pipeline {
  id: number
  projectId: number
  name: string
  description: string
  stages: StageConfig[]
  buildParams: Record<string, string>
  triggerOnPush: boolean
  triggerOnTag: boolean
  triggerOnSchedule: boolean
  cronExpression: string
  timeout: number
  status: number
  isDefault: boolean
}

/** 流水线阶段配置 */
export interface StageConfig {
  name: string
  type: 'GIT_CLONE' | 'MAVEN_BUILD' | 'NPM_BUILD' | 'PYTHON_BUILD' | 'DOCKER_BUILD'
  enabled?: boolean
  config?: Record<string, any>
}

// =====================================================
// API 方法
// =====================================================

/**
 * 获取构建列表
 */
export function getBuildList(params?: BuildQuery) {
  return get<{ records: Build[]; total: number; page: number; size: number }>('/builds', params)
}

/**
 * 获取构建详情
 */
export function getBuildDetail(id: number) {
  return get<Build>(`/builds/${id}`)
}

/**
 * 触发构建
 */
export function triggerBuild(data: BuildTriggerRequest) {
  return post<Build>('/builds', data)
}

/**
 * 取消构建
 */
export function cancelBuild(id: number) {
  return post(`/builds/${id}/cancel`)
}

/**
 * 获取构建日志
 */
export function getBuildLog(id: number) {
  return get<string>(`/builds/${id}/log`)
}

/**
 * 获取构建产物
 */
export function getBuildArtifact(id: number) {
  return get<Artifact[]>(`/builds/${id}/artifact`)
}

/**
 * 获取流水线列表
 */
export function getPipelineList(projectId: number) {
  return get<Pipeline[]>(`/projects/${projectId}/pipeline`)
}

/**
 * 创建流水线
 */
export function createPipeline(projectId: number, data: Partial<Pipeline>) {
  return post<Pipeline>(`/projects/${projectId}/pipeline`, data)
}

/**
 * 更新流水线
 */
export function updatePipeline(projectId: number, id: number, data: Partial<Pipeline>) {
  return put<Pipeline>(`/projects/${projectId}/pipeline/${id}`, data)
}

/**
 * 删除流水线
 */
export function deletePipeline(projectId: number, id: number) {
  return del(`/projects/${projectId}/pipeline/${id}`)
}

/**
 * 获取流水线详情
 */
export function getPipelineDetail(projectId: number, id: number) {
  return get<Pipeline>(`/projects/${projectId}/pipeline/${id}`)
}

// WebSocket URL 工厂函数
export function getBuildLogWsUrl(buildId: number) {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  return `${protocol}//${host}/ws/build/${buildId}/log`
}
