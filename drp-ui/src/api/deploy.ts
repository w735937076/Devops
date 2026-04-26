/**
 * DRP Platform - 部署管理模块 API
 *
 * @author Nick
 */

import { get, post } from './request'

export enum DeployStatus {
  WAIT_APPROVAL = 0,
  PENDING = 1,
  RUNNING = 2,
  SUCCESS = 3,
  FAILED = 4,
  CANCELLED = 5,
  ROLLED_BACK = 6,
  REJECTED = 7
}

export const DeployStatusMap: Record<number, { label: string; type: string }> = {
  [DeployStatus.WAIT_APPROVAL]: { label: '待审批', type: 'warning' },
  [DeployStatus.PENDING]: { label: '排队中', type: 'info' },
  [DeployStatus.RUNNING]: { label: '执行中', type: 'primary' },
  [DeployStatus.SUCCESS]: { label: '成功', type: 'success' },
  [DeployStatus.FAILED]: { label: '失败', type: 'danger' },
  [DeployStatus.CANCELLED]: { label: '已取消', type: 'info' },
  [DeployStatus.ROLLED_BACK]: { label: '已回滚', type: 'warning' },
  [DeployStatus.REJECTED]: { label: '已驳回', type: 'danger' }
}

export type DeployEnv = 'dev' | 'test' | 'pre' | 'prod'
export type DeployStrategy = 'SINGLE' | 'ROLLING' | 'BLUE_GREEN' | 'GRAY' | 'ROLLBACK'
export type ApprovalStatus = 'NOT_REQUIRED' | 'PENDING' | 'APPROVED' | 'REJECTED'
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'

export interface DeployServer {
  id: number
  name: string
  hostname: string
  env?: string
  deployPath?: string
  status?: number
  statusDesc?: string
}

export interface DeployCheckItem {
  name: string
  status: 'PASS' | 'FAIL' | 'WARN' | string
  message: string
}

export interface DeployStep {
  stepKey: string
  stepName: string
  status: string
  duration?: number
  detail?: string
}

export interface ApprovalRecord {
  id: number
  status: ApprovalStatus
  statusDesc: string
  approver?: string
  comment?: string
  approveTime?: string
  createTime?: string
}

export interface DeploySummary {
  id: number
  projectId: number
  projectName: string
  buildId: number
  buildNumber: number
  version: string
  branch: string
  env: DeployEnv
  strategy: string
  status: DeployStatus
  statusDesc: string
  approvalStatus: ApprovalStatus
  approvalStatusDesc: string
  riskLevel: RiskLevel
  riskLevelDesc: string
  triggerType: string
  triggerTypeDesc: string
  triggerUser: string
  currentStep: string
  duration?: number
  serverNames: string[]
  startTime?: string
  endTime?: string
  createTime?: string
  updateTime?: string
}

export interface DeployDetail extends DeploySummary {
  deployWindow?: string
  windowValid?: boolean
  requireApproval?: boolean
  autoRollback?: boolean
  grayPercent?: number
  grayInterval?: number
  changeTicket?: string
  errorMessage?: string
  summary?: string
  logContent?: string
  rollbackFromDeployId?: number
  servers: DeployServer[]
  healthChecks: DeployCheckItem[]
  steps: DeployStep[]
  approvalRecords: ApprovalRecord[]
}

export interface DeployPreview {
  projectId: number
  projectName: string
  buildId: number
  buildNumber: number
  version: string
  branch: string
  env: DeployEnv
  strategy: string
  riskLevel: RiskLevel
  riskLevelDesc: string
  requireApproval: boolean
  windowValid: boolean
  deployWindow?: string
  grayPercent?: number
  grayInterval?: number
  hasRunningDeploy: boolean
  servers: DeployServer[]
  checks: DeployCheckItem[]
  warnings: string[]
}

export interface DeployQuery {
  projectId?: number
  env?: DeployEnv
  status?: number
  approvalStatus?: ApprovalStatus
  riskLevel?: RiskLevel
  triggerType?: string
  page?: number
  pageSize?: number
}

export interface DeployPreviewRequest {
  projectId: number
  buildId: number
  env: DeployEnv
  strategy: DeployStrategy
  serverIds?: number[]
  requireApproval?: boolean
  deployWindow?: string
  grayPercent?: number
  grayInterval?: number
  triggerType?: string
  changeTicket?: string
  autoRollback?: boolean
}

export interface DeployCreateRequest extends DeployPreviewRequest {}

export function getDeployList(params?: DeployQuery) {
  return get<{ records: DeploySummary[]; total: number; page: number; size: number }>('/deploys', params)
}

export function getDeployDetail(id: number) {
  return get<DeployDetail>(`/deploys/${id}`)
}

export function getPendingDeployApprovals() {
  return get<DeploySummary[]>('/deploys/pending-approvals')
}

export function previewDeploy(data: DeployPreviewRequest) {
  return post<DeployPreview>('/deploys/preview', data)
}

export function createDeploy(data: DeployCreateRequest) {
  return post<DeployDetail>('/deploys', data)
}

export function cancelDeploy(id: number) {
  return post(`/deploys/${id}/cancel`)
}

export function retryDeploy(id: number) {
  return post<DeployDetail>(`/deploys/${id}/retry`)
}

export function rollbackDeploy(id: number, reason?: string) {
  return post<DeployDetail>(`/deploys/${id}/rollback`, { reason })
}

export function approveDeploy(id: number, comment?: string) {
  return post<DeployDetail>(`/deploys/${id}/approval/approve`, { comment })
}

export function rejectDeploy(id: number, comment?: string) {
  return post<DeployDetail>(`/deploys/${id}/approval/reject`, { comment })
}

export function getDeployLog(id: number) {
  return get<string>(`/deploys/${id}/log`)
}

export function getDeployHealth(id: number) {
  return get<DeployCheckItem[]>(`/deploys/${id}/health`)
}
