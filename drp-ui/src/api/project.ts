/**
 * DRP Platform - 项目管理模块 API
 *
 * @author Nick
 */

import { get, post, put, del } from './request'

// =====================================================
// 类型定义
// =====================================================

/** 项目信息 */
export interface Project {
  id: number
  name: string
  code: string
  description: string
  gitUrl: string
  gitType: string
  buildTool: string
  deployType: string
  status: number
  createTime: string
  updateTime: string
}

/** 项目列表查询参数 */
export interface ProjectQuery {
  keyword?: string
  status?: number
  page?: number
  pageSize?: number
}

/** 创建项目参数 */
export interface CreateProjectParams {
  name: string
  code: string
  description?: string
  gitUrl: string
  gitType?: string
  buildTool?: string
  deployType?: string
}

// =====================================================
// API 方法
// =====================================================

/**
 * 获取项目列表
 */
export function getProjectList(params?: ProjectQuery) {
  return get<{ list: Project[]; total: number }>('/projects', params)
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
export function updateProject(id: number, data: Partial<CreateProjectParams>) {
  return put<Project>(`/projects/${id}`, data)
}

/**
 * 删除项目
 */
export function deleteProject(id: number) {
  return del(`/projects/${id}`)
}

/**
 * 获取项目环境变量
 */
export function getProjectVariables(projectId: number) {
  return get<{ key: string; value: string }[]>(`/projects/${projectId}/variables`)
}
