/**
 * DRP Platform - 服务器管理模块 API
 *
 * @author Nick
 */

import request, { get } from './request'
import type { PageResponse } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 服务器状态 */
export enum ServerStatus {
  OFFLINE = 0,
  ONLINE = 1,
  BUSY = 2
}

/** 服务器状态描述 */
export const ServerStatusDesc: Record<number, string> = {
  [ServerStatus.OFFLINE]: '离线',
  [ServerStatus.ONLINE]: '在线',
  [ServerStatus.BUSY]: '忙碌'
}

/** 服务器信息 */
export interface Server {
  id: number
  name: string
  hostname: string
  port: number
  username: string
  password?: string
  privateKey?: string
  privateKeyPassphrase?: string
  groups?: string
  env?: string
  tags?: string
  remark?: string
  workDir?: string
  backupDir?: string
  status: number
  statusDesc?: string
  lastHeartbeat?: string
  createTime?: string
  updateTime?: string
}

/** 服务器分组 */
export interface ServerGroup {
  id: number
  name: string
  code: string
  description?: string
  sort?: number
}

/** 服务器查询参数 */
export interface ServerQuery {
  keyword?: string
  group?: string
  status?: number
  page?: number
  pageSize?: number
}

/** 创建服务器参数 */
export interface CreateServerParams {
  name: string
  hostname: string
  port?: number
  username: string
  password?: string
  privateKey?: string
  privateKeyPassphrase?: string
  groups?: string
  env?: string
  tags?: string
  remark?: string
  workDir?: string
  backupDir?: string
}

/** 更新服务器参数 */
export interface UpdateServerParams extends CreateServerParams {}

/** 连接测试结果 */
export interface ConnectionTestResult {
  hostname: string
  port: number
  success: boolean
  message: string
  costMs: number
}

/** 创建分组参数 */
export interface CreateGroupParams {
  name: string
  code: string
  description?: string
  sort?: number
}

/** 更新分组参数 */
export interface UpdateGroupParams {
  name: string
  description?: string
  sort?: number
}

// =====================================================
// API 方法 - 服务器
// =====================================================

/**
 * 获取服务器列表
 */
export function getServerList(params?: ServerQuery) {
  return request.get<PageResponse<Server>>('/servers', { params })
}

/**
 * 获取所有服务器
 */
export function getServerAll() {
  return get<Server[]>('/servers/all')
}

/**
 * 获取服务器详情
 */
export function getServerDetail(id: number) {
  return request.get<Server>(`/servers/${id}`)
}

/**
 * 创建服务器
 */
export function createServer(data: CreateServerParams) {
  return request.post<Server>('/servers', data)
}

/**
 * 更新服务器
 */
export function updateServer(id: number, data: UpdateServerParams) {
  return request.put<Server>(`/servers/${id}`, data)
}

/**
 * 删除服务器
 */
export function deleteServer(id: number) {
  return request.delete(`/servers/${id}`)
}

/**
 * 测试服务器连接
 */
export function testServerConnection(id: number) {
  return request.post<ConnectionTestResult>(`/servers/${id}/test`)
}

// =====================================================
// API 方法 - 服务器分组
// =====================================================

/**
 * 获取服务器分组列表
 */
export function getServerGroupList() {
  return request.get<ServerGroup[]>('/server-groups')
}

/**
 * 获取服务器分组详情
 */
export function getServerGroupDetail(id: number) {
  return request.get<ServerGroup>(`/server-groups/${id}`)
}

/**
 * 创建服务器分组
 */
export function createServerGroup(data: CreateGroupParams) {
  return request.post<ServerGroup>('/server-groups', data)
}

/**
 * 更新服务器分组
 */
export function updateServerGroup(id: number, data: UpdateGroupParams) {
  return request.put<ServerGroup>(`/server-groups/${id}`, data)
}

/**
 * 删除服务器分组
 */
export function deleteServerGroup(id: number) {
  return request.delete(`/server-groups/${id}`)
}
