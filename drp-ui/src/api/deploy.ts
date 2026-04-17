/**
 * DRP Platform - 部署管理模块 API
 *
 * @author Nick
 */

import { get, post, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 部署状态枚举 */
export enum DeployStatus {
  PENDING = 0,
  RUNNING = 1,
  SUCCESS = 2,
  FAILED = 3,
  ROLLBACK = 4
}

/** 部署环境枚举 */
export enum DeployEnv {
  DEV = 'dev',
  TEST = 'test',
  PRE = 'pre',
  PROD = 'prod'
}

/** 部署记录 */
export interface Deploy {
  id: number
  projectId: number
  projectName: string
  buildId: number
  buildNumber: number
  version: string
  env: DeployEnv
  status: DeployStatus
  strategy: string
  servers: string[]
  duration: number
  deployUser: string
  createTime: string
  updateTime: string
}

/** 部署查询参数 */
export interface DeployQuery {
  projectId?: number
  env?: DeployEnv
  status?: DeployStatus
  page?: number
  pageSize?: number
}

/** 创建部署参数 */
export interface CreateDeployParams {
  projectId: number
  buildId: number
  env: DeployEnv
  strategy: string
  serverIds: number[]
}

// =====================================================
// API 方法
// =====================================================

/**
 * 获取部署列表
 */
export function getDeployList(params?: DeployQuery) {
  return get<{ list: Deploy[]; total: number }>('/deploys', params)
}

/**
 * 获取部署详情
 */
export function getDeployDetail(id: number) {
  return get<Deploy>(`/deploys/${id}`)
}

/**
 * 创建部署
 */
export function createDeploy(data: CreateDeployParams) {
  return post<Deploy>('/deploys', data)
}

/**
 * 执行部署
 */
export function executeDeploy(id: number) {
  return post(`/deploys/${id}/execute`)
}

/**
 * 回滚部署
 */
export function rollbackDeploy(id: number) {
  return post(`/deploys/${id}/rollback`)
}

/**
 * 获取部署日志
 */
export function getDeployLog(id: number) {
  return get<string>(`/deploys/${id}/log`)
}
