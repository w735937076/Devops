/**
 * 角色管理 API
 */

import { get, post, put, del } from './request'

/** 角色查询参数 */
export interface RoleQueryParams {
  page?: number
  size?: number
  code?: string
  name?: string
  type?: string
  status?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

/** 角色信息 */
export interface Role {
  id: number
  code: string
  name: string
  description: string
  type: string
  sort: number
  status: number
  statusDesc: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
  permissionIds?: number[]
}

/** 创建角色请求 */
export interface CreateRoleRequest {
  code: string
  name: string
  description?: string
  type?: string
  sort?: number
  status?: number
}

/** 更新角色请求 */
export interface UpdateRoleRequest {
  id: number
  name?: string
  description?: string
  sort?: number
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
 * 分页查询角色
 */
export function queryRoles(params: RoleQueryParams): Promise<PageResponse<Role>> {
  return get('/roles', params)
}

/**
 * 获取所有角色
 */
export function getAllRoles(): Promise<Role[]> {
  return get('/roles/all')
}

/**
 * 获取角色详情
 */
export function getRole(id: number): Promise<Role> {
  return get(`/roles/${id}`)
}

/**
 * 根据编码获取角色
 */
export function getRoleByCode(code: string): Promise<Role> {
  return get(`/roles/code/${code}`)
}

/**
 * 创建角色
 */
export function createRole(data: CreateRoleRequest): Promise<Role> {
  return post('/roles', data)
}

/**
 * 更新角色
 */
export function updateRole(data: UpdateRoleRequest): Promise<Role> {
  return put(`/roles/${data.id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: number): Promise<void> {
  return del(`/roles/${id}`)
}

/**
 * 分配角色权限
 */
export function assignRolePermissions(id: number, permissionIds: number[]): Promise<void> {
  return put(`/roles/${id}/permissions`, { permissionIds })
}

/**
 * 获取角色权限ID列表
 */
export function getRolePermissionIds(id: number): Promise<number[]> {
  return get(`/roles/${id}/permissions`)
}