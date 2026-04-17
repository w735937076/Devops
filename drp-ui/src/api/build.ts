/**
 * DRP Platform - 构建管理模块 API
 *
 * @author Nick
 */

import { get, post, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 构建状态枚举 */
export enum BuildStatus {
  PENDING = 0,
  RUNNING = 1,
  SUCCESS = 2,
  FAILED = 3,
  CANCELLED = 4
}

/** 构建记录 */
export interface Build {
  id: number
  projectId: number
  projectName: string
  branch: string
  commitId: string
  commitMessage: string
  buildNumber: number
  status: BuildStatus
  duration: number
  artifacts: string[]
  triggerType: string
  triggerUser: string
  createTime: string
  updateTime: string
}

/** 构建查询参数 */
export interface BuildQuery {
  projectId?: number
  status?: BuildStatus
  branch?: string
  page?: number
  pageSize?: number
}

// =====================================================
// API 方法
// =====================================================

/**
 * 获取构建列表
 */
export function getBuildList(params?: BuildQuery) {
  return get<{ list: Build[]; total: number }>('/builds', params)
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
export function triggerBuild(projectId: number, branch: string) {
  return post<Build>('/builds/trigger', { projectId, branch })
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
