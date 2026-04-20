/**
 * DRP Platform - 项目管理模块 API
 *
 * @author Nick
 */

import { get, post, put, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 项目类型 */
export type ProjectType = 'JAVA_MAVEN' | 'NODE' | 'PYTHON'

/** 项目状态 */
export type ProjectStatus = 0 | 1

/** 项目信息 */
export interface Project {
  id: number
  name: string
  code: string
  type: ProjectType
  typeDesc: string
  description: string
  gitUrl: string
  credentialId: number | null
  credentialName: string | null
  defaultBranch: string
  buildConfig: string | null
  status: ProjectStatus
  statusDesc: string
  createTime: string
  updateTime: string
}

/** 项目列表查询参数 */
export interface ProjectQuery {
  keyword?: string
  type?: string
  status?: number
  page?: number
  pageSize?: number
}

/** 创建项目参数 */
export interface CreateProjectParams {
  name: string
  code: string
  type: ProjectType
  description?: string
  gitUrl: string
  credentialId?: number | null
  defaultBranch?: string
  buildConfig?: string
  status?: ProjectStatus
}

/** 更新项目参数 */
export interface UpdateProjectParams {
  name: string
  type: ProjectType
  description?: string
  gitUrl: string
  credentialId?: number | null
  defaultBranch?: string
  buildConfig?: string
  status?: ProjectStatus
}

// =====================================================
// API 方法
// =====================================================

/**
 * 分页查询项目
 */
export function getProjectPage(params?: ProjectQuery) {
  return get<{ records: Project[]; total: number }>('/projects', params)
}

/**
 * 获取项目详情
 */
export function getProjectDetail(id: number) {
  return get<Project>(`/projects/${id}`)
}

/**
 * 创建项目
 */
export function createProject(data: CreateProjectParams) {
  return post<Project>('/projects', data)
}

/**
 * 更新项目
 */
export function updateProject(id: number, data: UpdateProjectParams) {
  return put<Project>(`/projects/${id}`, data)
}

/**
 * 删除项目
 */
export function deleteProject(id: number) {
  return del(`/projects/${id}`)
}