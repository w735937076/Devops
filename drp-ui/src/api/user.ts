/**
 * 用户管理 API
 */

import { get, post, put, del } from './request'

/** 用户查询参数 */
export interface UserQueryParams {
  page?: number
  size?: number
  username?: string
  realName?: string
  email?: string
  phone?: string
  status?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

/** 用户信息 */
export interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  status: number
  statusDesc: string
  createTime: string
  createBy: string
  roles?: UserRole[]
}

/** 用户角色信息 */
export interface UserRole {
  id: number
  code: string
  name: string
}

/** 创建用户请求 */
export interface CreateUserRequest {
  username: string
  password: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  roleIds?: number[]
}

/** 更新用户请求 */
export interface UpdateUserRequest {
  id: number
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  roleIds?: number[]
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
 * 分页查询用户
 */
export function queryUsers(params: UserQueryParams): Promise<PageResponse<User>> {
  return get('/users', params)
}

/**
 * 获取用户详情
 */
export function getUser(id: number): Promise<User> {
  return get(`/users/${id}`)
}

/**
 * 创建用户
 */
export function createUser(data: CreateUserRequest): Promise<User> {
  return post('/users', data)
}

/**
 * 更新用户
 */
export function updateUser(data: UpdateUserRequest): Promise<User> {
  return put(`/users/${data.id}`, data)
}

/**
 * 删除用户
 */
export function deleteUser(id: number): Promise<void> {
  return del(`/users/${id}`)
}

/**
 * 分配用户角色
 */
export function assignUserRoles(id: number, roleIds: number[]): Promise<void> {
  return put(`/users/${id}/roles`, { roleIds })
}

/**
 * 修改密码
 */
export function changePassword(id: number, oldPassword: string, newPassword: string): Promise<void> {
  return put(`/users/${id}/password`, { oldPassword, newPassword })
}

/**
 * 重置密码
 */
export function resetPassword(id: number, newPassword: string): Promise<void> {
  return put(`/users/${id}/reset-password`, { newPassword })
}