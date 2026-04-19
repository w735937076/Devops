/**
 * DRP Platform - 凭证管理模块 API
 *
 * @author Nick
 */

import { get, post, put, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 凭证类型枚举 */
export type CredentialType = 'USERNAME_PASSWORD' | 'ACCESS_TOKEN' | 'SSH_KEY'

/** 凭证信息 */
export interface Credential {
  id: number
  name: string
  type: CredentialType
  typeDesc: string
  account: string
  secretMasked: string
  description: string
  referencedProjectCount: number
  createTime: string
  updateTime: string
}

/** 凭证列表查询参数 */
export interface CredentialQuery {
  keyword?: string
  type?: string
  page?: number
  pageSize?: number
}

/** 创建/更新凭证参数 */
export interface CredentialParams {
  name: string
  type: CredentialType
  account?: string
  secretContent?: string
  description?: string
}

// =====================================================
// API 方法
// =====================================================

/**
 * 分页查询凭证
 */
export function getCredentialPage(params?: CredentialQuery) {
  return get<{ records: Credential[]; total: number }>('/credentials', params)
}

/**
 * 获取凭证详情
 */
export function getCredentialDetail(id: number) {
  return get<Credential>(`/credentials/${id}`)
}

/**
 * 创建凭证
 */
export function createCredential(data: CredentialParams) {
  return post<Credential>('/credentials', data)
}

/**
 * 更新凭证
 */
export function updateCredential(id: number, data: CredentialParams) {
  return put<Credential>(`/credentials/${id}`, data)
}

/**
 * 删除凭证
 */
export function deleteCredential(id: number) {
  return del(`/credentials/${id}`)
}
