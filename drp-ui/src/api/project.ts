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

// =====================================================
// 环境变量管理
// =====================================================

/** 环境变量信息 */
export interface EnvVariable {
  id: number
  projectId: number
  envCode: string
  envName: string
  varKey: string
  varValue: string
  isSecret: number
  isSecretDesc: string
  createTime: string
}

/** 创建环境变量参数 */
export interface CreateEnvVariableParams {
  envCode: 'dev' | 'test' | 'prod'
  varKey: string
  varValue: string
  isSecret?: number
}

/** 更新环境变量参数 */
export interface UpdateEnvVariableParams {
  envCode: 'dev' | 'test' | 'prod'
  varKey: string
  varValue: string
  isSecret: number
}

/**
 * 获取项目环境变量列表
 */
export function getEnvVariables(projectId: number, env?: string) {
  return get<EnvVariable[]>(`/projects/${projectId}/variables`, { env })
}

/**
 * 创建环境变量
 */
export function createEnvVariable(projectId: number, data: CreateEnvVariableParams) {
  return post<EnvVariable>(`/projects/${projectId}/variables`, data)
}

/**
 * 更新环境变量
 */
export function updateEnvVariable(projectId: number, varId: number, data: UpdateEnvVariableParams) {
  return put<EnvVariable>(`/projects/${projectId}/variables/${varId}`, data)
}

/**
 * 删除环境变量
 */
export function deleteEnvVariable(projectId: number, varId: number) {
  return del(`/projects/${projectId}/variables/${varId}`)
}

// =====================================================
// 分支策略管理
// =====================================================

/** 分支策略信息 */
export interface BranchPolicy {
  id: number
  projectId: number
  branchPattern: string
  allowAutoDeploy: number
  allowAutoDeployDesc: string
  requireApproval: number
  requireApprovalDesc: string
  createTime: string
}

/** 创建分支策略参数 */
export interface CreateBranchPolicyParams {
  branchPattern: string
  allowAutoDeploy?: number
  requireApproval?: number
}

/** 更新分支策略参数 */
export interface UpdateBranchPolicyParams {
  branchPattern: string
  allowAutoDeploy: number
  requireApproval: number
}

/**
 * 获取项目分支策略列表
 */
export function getBranchPolicies(projectId: number) {
  return get<BranchPolicy[]>(`/projects/${projectId}/branch-policies`)
}

/**
 * 创建分支策略
 */
export function createBranchPolicy(projectId: number, data: CreateBranchPolicyParams) {
  return post<BranchPolicy>(`/projects/${projectId}/branch-policies`, data)
}

/**
 * 更新分支策略
 */
export function updateBranchPolicy(projectId: number, policyId: number, data: UpdateBranchPolicyParams) {
  return put<BranchPolicy>(`/projects/${projectId}/branch-policies/${policyId}`, data)
}

/**
 * 删除分支策略
 */
export function deleteBranchPolicy(projectId: number, policyId: number) {
  return del(`/projects/${projectId}/branch-policies/${policyId}`)
}

// =====================================================
// 项目部署服务器绑定
// =====================================================

export interface ProjectDeployServer {
  id: number
  projectId: number
  serverId: number
  serverName: string
  hostname: string
  port?: number
  serverEnv?: string
  serverStatus?: number
  serverStatusDesc?: string
  env: 'dev' | 'test' | 'pre' | 'prod'
  envName: string
  deployPath: string
  createTime: string
}

export interface CreateProjectDeployServerParams {
  serverId: number
  env: 'dev' | 'test' | 'pre' | 'prod'
  deployPath: string
}

export interface UpdateProjectDeployServerParams extends CreateProjectDeployServerParams {}

export function getProjectDeployServers(projectId: number, env?: string) {
  return get<ProjectDeployServer[]>(`/projects/${projectId}/deploy-servers`, { env })
}

export function createProjectDeployServer(projectId: number, data: CreateProjectDeployServerParams) {
  return post<ProjectDeployServer>(`/projects/${projectId}/deploy-servers`, data)
}

export function updateProjectDeployServer(projectId: number, bindingId: number, data: UpdateProjectDeployServerParams) {
  return put<ProjectDeployServer>(`/projects/${projectId}/deploy-servers/${bindingId}`, data)
}

export function deleteProjectDeployServer(projectId: number, bindingId: number) {
  return del(`/projects/${projectId}/deploy-servers/${bindingId}`)
}
