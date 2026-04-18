/**
 * 权限管理 API
 */

import { get, post, put, del } from './request'

/** 权限查询参数 */
export interface PermissionQueryParams {
  page?: number
  size?: number
  code?: string
  name?: string
  type?: string
  status?: number
}

/** 权限信息 */
export interface Permission {
  id: number
  code: string
  name: string
  description: string
  parentId: number | null
  parentName: string
  type: string
  sort: number
  icon: string
  path: string
  component: string
  status: number
  statusDesc: string
  createTime: string
  createBy: string
  children?: Permission[]
}

/** 创建权限请求 */
export interface CreatePermissionRequest {
  code: string
  name: string
  description?: string
  parentId?: number
  type?: string
  sort?: number
  icon?: string
  path?: string
  component?: string
  status?: number
}

/** 更新权限请求 */
export interface UpdatePermissionRequest {
  id: number
  name?: string
  description?: string
  parentId?: number
  sort?: number
  icon?: string
  path?: string
  component?: string
  status?: number
}

/** 分页响应 */
export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  size: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

/**
 * 分页查询权限
 */
export function queryPermissions(params: PermissionQueryParams): Promise<PageResponse<Permission>> {
  return get('/permissions', params)
}

/**
 * 获取所有权限（树形结构）
 */
export function getPermissionTree(): Promise<Permission[]> {
  return get('/permissions/tree')
}

/**
 * 获取所有权限（扁平列表）
 */
export function getAllPermissions(): Promise<Permission[]> {
  return get('/permissions/all')
}

/**
 * 获取权限详情
 */
export function getPermission(id: number): Promise<Permission> {
  return get(`/permissions/${id}`)
}

/**
 * 根据编码获取权限
 */
export function getPermissionByCode(code: string): Promise<Permission> {
  return get(`/permissions/code/${code}`)
}

/**
 * 创建权限
 */
export function createPermission(data: CreatePermissionRequest): Promise<Permission> {
  return post('/permissions', data)
}

/**
 * 更新权限
 */
export function updatePermission(data: UpdatePermissionRequest): Promise<Permission> {
  return put(`/permissions/${data.id}`, data)
}

/**
 * 删除权限
 */
export function deletePermission(id: number): Promise<void> {
  return del(`/permissions/${id}`)
}

/**
 * 根据角色ID获取权限ID列表
 */
export function getPermissionIdsByRoleId(roleId: number): Promise<number[]> {
  return get(`/permissions/role/${roleId}`)
}