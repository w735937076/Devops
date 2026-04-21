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

/**
 * 获取 Git 仓库分支列表
 */
export function fetchGitBranches(gitUrl: string, credentialId?: number | null) {
  return get<string[]>('/projects/git/branches', { gitUrl, credentialId })
}

// =====================================================
// 项目成员管理
// =====================================================

/** 项目成员角色 */
export type MemberRole = 'OWNER' | 'DEVELOPER' | 'REPORTER'

/** 项目成员信息 */
export interface ProjectMember {
  id: number
  projectId: number
  userId: number
  username: string
  realName: string
  role: MemberRole
  roleDesc: string
  permission: string
  createTime: string
}

/** 添加项目成员参数 */
export interface AddMemberParams {
  userId: number
  role: MemberRole
}

/** 更新成员角色参数 */
export interface UpdateMemberParams {
  role: MemberRole
}

/**
 * 获取项目成员列表
 */
export function getProjectMembers(projectId: number) {
  return get<ProjectMember[]>(`/projects/${projectId}/members`)
}

/**
 * 添加项目成员
 */
export function addProjectMember(projectId: number, data: AddMemberParams) {
  return post<ProjectMember>(`/projects/${projectId}/members`, data)
}

/**
 * 更新成员角色
 */
export function updateProjectMember(projectId: number, userId: number, data: UpdateMemberParams) {
  return put<ProjectMember>(`/projects/${projectId}/members/${userId}`, data)
}

/**
 * 移除项目成员
 */
export function removeProjectMember(projectId: number, userId: number) {
  return del(`/projects/${projectId}/members/${userId}`)
}

// =====================================================
// 用户管理（用于项目成员选择）
// =====================================================

/** 用户信息（简化版） */
export interface SimpleUser {
  id: number
  username: string
  realName: string
}

/**
 * 获取可选用户列表（用于添加项目成员）
 */
export function getAvailableUsers() {
  return get<SimpleUser[]>('/users/simple-list')
}