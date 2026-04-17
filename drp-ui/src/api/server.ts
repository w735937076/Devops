/**
 * DRP Platform - 服务器管理模块 API
 *
 * @author Nick
 */

import { get, post, put, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 服务器状态 */
export enum ServerStatus {
  OFFLINE = 0,
  ONLINE = 1
}

/** 服务器信息 */
export interface Server {
  id: number
  name: string
  ip: string
  port: number
  username: string
  description: string
  env: string
  status: ServerStatus
  lastHeartbeat: string
  createTime: string
}

/** 服务器查询参数 */
export interface ServerQuery {
  keyword?: string
  env?: string
  status?: ServerStatus
  page?: number
  pageSize?: number
}

/** 创建服务器参数 */
export interface CreateServerParams {
  name: string
  ip: string
  port: number
  username: string
  password?: string
  description?: string
  env: string
}

// =====================================================
// API 方法
// =====================================================

/**
 * 获取服务器列表
 */
export function getServerList(params?: ServerQuery) {
  return get<{ list: Server[]; total: number }>('/servers', params)
}

/**
 * 获取服务器详情
 */
export function getServerDetail(id: number) {
  return get<Server>(`/servers/${id}`)
}

/**
 * 创建服务器
 */
export function createServer(data: CreateServerParams) {
  return post<Server>('/servers', data)
}

/**
 * 更新服务器
 */
export function updateServer(id: number, data: Partial<CreateServerParams>) {
  return put<Server>(`/servers/${id}`, data)
}

/**
 * 删除服务器
 */
export function deleteServer(id: number) {
  return del(`/servers/${id}`)
}

/**
 * 测试服务器连接
 */
export function testServerConnection(id: number) {
  return post<{ success: boolean; message: string }>(`/servers/${id}/test`)
}
