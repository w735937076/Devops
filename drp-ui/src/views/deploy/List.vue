<template>
  <div class="page-container">
    <div class="query-card card-container">
      <div class="page-header">
        <div>
          <h2 class="page-title">部署记录</h2>
          <p class="page-desc">统一管理部署预览、审批、回滚与运行结果</p>
        </div>
        <el-button type="primary" @click="$router.push('/deploys/create')">创建部署</el-button>
      </div>

      <div class="filter-bar">
        <div class="filter-row">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="项目">
              <el-select v-model="queryParams.projectId" placeholder="全部项目" clearable>
                <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="环境">
              <el-select v-model="queryParams.env" placeholder="全部环境" clearable>
                <el-option label="开发" value="dev" />
                <el-option label="测试" value="test" />
                <el-option label="预发" value="pre" />
                <el-option label="生产" value="prod" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
                <el-option v-for="(item, key) in DeployStatusMap" :key="key" :label="item.label" :value="Number(key)" />
              </el-select>
            </el-form-item>
            <el-form-item label="审批">
              <el-select v-model="queryParams.approvalStatus" placeholder="全部审批" clearable>
                <el-option label="待审批" value="PENDING" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已驳回" value="REJECTED" />
                <el-option label="不需要" value="NOT_REQUIRED" />
              </el-select>
            </el-form-item>
            <el-form-item label="风险">
              <el-select v-model="queryParams.riskLevel" placeholder="全部风险" clearable>
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ total }}</div>
        <div class="stat-label">部署总数</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-value">{{ pendingApprovals.length }}</div>
        <div class="stat-label">待审批</div>
      </div>
      <div class="stat-card success">
        <div class="stat-value">{{ successCount }}</div>
        <div class="stat-label">成功部署</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-value">{{ failedCount }}</div>
        <div class="stat-label">失败/驳回</div>
      </div>
    </div>

    <div class="content-grid">
      <div class="card-container table-card">
        <el-table :data="deployList" v-loading="loading" stripe>
          <el-table-column prop="projectName" label="项目" min-width="120" />
          <el-table-column prop="version" label="版本" min-width="180" />
          <el-table-column prop="env" label="环境" width="90">
            <template #default="{ row }">
              <el-tag :type="getEnvType(row.env)">{{ getEnvLabel(row.env) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="strategy" label="策略" width="100" />
          <el-table-column prop="approvalStatusDesc" label="审批" width="100">
            <template #default="{ row }">
              <el-tag :type="getApprovalType(row.approvalStatus)">{{ row.approvalStatusDesc }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="riskLevelDesc" label="风险" width="80">
            <template #default="{ row }">
              <el-tag :type="getRiskType(row.riskLevel)">{{ row.riskLevelDesc }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="statusDesc" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="DeployStatusMap[row.status]?.type">{{ row.statusDesc }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="currentStep" label="当前阶段" min-width="120" />
          <el-table-column prop="triggerUser" label="触发人" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
              <el-button link type="warning" v-if="row.approvalStatus === 'PENDING'" @click="openApproval(row, true)">通过</el-button>
              <el-button link type="danger" v-if="row.approvalStatus === 'PENDING'" @click="openApproval(row, false)">驳回</el-button>
              <el-button link type="warning" v-if="row.status === DeployStatus.RUNNING || row.status === DeployStatus.PENDING || row.status === DeployStatus.WAIT_APPROVAL" @click="handleCancel(row.id)">取消</el-button>
              <el-button link type="danger" v-if="row.status === DeployStatus.FAILED || row.status === DeployStatus.REJECTED || row.status === DeployStatus.CANCELLED" @click="handleRetry(row.id)">重试</el-button>
              <el-button link type="danger" v-if="row.status === DeployStatus.SUCCESS" @click="handleRollback(row.id)">回滚</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @change="loadData"
          />
        </div>
      </div>

      <div class="card-container pending-panel">
        <div class="panel-header">待审批发布</div>
        <div v-if="pendingApprovals.length" class="pending-list">
          <div v-for="item in pendingApprovals" :key="item.id" class="pending-item">
            <div class="pending-title">{{ item.projectName }} / {{ getEnvLabel(item.env) }}</div>
            <div class="pending-meta">{{ item.version }}</div>
            <div class="pending-meta">策略：{{ item.strategy }} | 风险：{{ item.riskLevelDesc }}</div>
            <div class="pending-actions">
              <el-button size="small" type="success" @click="openApproval(item, true)">通过</el-button>
              <el-button size="small" type="danger" @click="openApproval(item, false)">驳回</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无待审批记录" />
      </div>
    </div>

    <el-dialog v-model="approvalDialog.visible" :title="approvalDialog.approve ? '审批通过' : '驳回部署'" width="420px">
      <el-input v-model="approvalDialog.comment" type="textarea" :rows="4" placeholder="请输入审批意见" />
      <template #footer>
        <el-button @click="approvalDialog.visible = false">取消</el-button>
        <el-button :type="approvalDialog.approve ? 'success' : 'danger'" @click="submitApproval">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectPage, type Project } from '@/api/project'
import {
  approveDeploy,
  cancelDeploy,
  DeployStatus,
  DeployStatusMap,
  getDeployList,
  getPendingDeployApprovals,
  rejectDeploy,
  retryDeploy,
  rollbackDeploy,
  type DeployQuery,
  type DeploySummary
} from '@/api/deploy'

const router = useRouter()
const loading = ref(false)
const deployList = ref<DeploySummary[]>([])
const pendingApprovals = ref<DeploySummary[]>([])
const projectList = ref<Project[]>([])
const total = ref(0)

const queryParams = reactive<DeployQuery>({
  projectId: undefined,
  env: undefined,
  status: undefined,
  approvalStatus: undefined,
  riskLevel: undefined,
  page: 1,
  pageSize: 10
})

const approvalDialog = reactive({
  visible: false,
  approve: true,
  deployId: 0,
  comment: ''
})

const successCount = computed(() => deployList.value.filter(item => item.status === DeployStatus.SUCCESS).length)
const failedCount = computed(() => deployList.value.filter(item => [DeployStatus.FAILED, DeployStatus.REJECTED].includes(item.status)).length)

onMounted(() => {
  loadProjects()
  loadData()
})

async function loadProjects() {
  const data = await getProjectPage({ page: 1, pageSize: 100 })
  projectList.value = data.records || []
}

async function loadData() {
  loading.value = true
  try {
    const [listData, approvalData] = await Promise.all([
      getDeployList(queryParams),
      getPendingDeployApprovals()
    ])
    deployList.value = listData.records || []
    total.value = listData.total || 0
    pendingApprovals.value = approvalData || []
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.projectId = undefined
  queryParams.env = undefined
  queryParams.status = undefined
  queryParams.approvalStatus = undefined
  queryParams.riskLevel = undefined
  queryParams.page = 1
  loadData()
}

function getEnvLabel(env?: string) {
  return ({ dev: '开发', test: '测试', pre: '预发', prod: '生产' } as Record<string, string>)[env || ''] || env || '-'
}

function getEnvType(env?: string) {
  return ({ dev: 'success', test: 'warning', pre: 'info', prod: 'danger' } as Record<string, string>)[env || ''] || 'info'
}

function getApprovalType(status: string) {
  return ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', NOT_REQUIRED: 'info' } as Record<string, string>)[status] || 'info'
}

function getRiskType(level: string) {
  return ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' } as Record<string, string>)[level] || 'info'
}

function goDetail(id: number) {
  router.push(`/deploys/${id}`)
}

function openApproval(row: DeploySummary, approve: boolean) {
  approvalDialog.visible = true
  approvalDialog.approve = approve
  approvalDialog.deployId = row.id
  approvalDialog.comment = ''
}

async function submitApproval() {
  if (!approvalDialog.deployId) return
  if (approvalDialog.approve) {
    await approveDeploy(approvalDialog.deployId, approvalDialog.comment)
    ElMessage.success('审批通过')
  } else {
    await rejectDeploy(approvalDialog.deployId, approvalDialog.comment)
    ElMessage.success('已驳回')
  }
  approvalDialog.visible = false
  loadData()
}

async function handleCancel(id: number) {
  await ElMessageBox.confirm('确定取消该部署吗？', '提示', { type: 'warning' })
  await cancelDeploy(id)
  ElMessage.success('部署已取消')
  loadData()
}

async function handleRetry(id: number) {
  await retryDeploy(id)
  ElMessage.success('已发起重试')
  loadData()
}

async function handleRollback(id: number) {
  const { value } = await ElMessageBox.prompt('请输入回滚原因', '回滚确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  await rollbackDeploy(id, value)
  ElMessage.success('回滚任务已创建')
  loadData()
}
</script>

<style scoped lang="scss">
.query-card {
  margin-bottom: 16px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.page-desc {
  color: #909399;
  margin: 6px 0 0;
  font-size: 13px;
}

.filter-bar {
  border-top: 1px solid #f0f2f5;
  padding-top: 16px;
  margin-top: 4px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 8px;
  margin-right: 16px;
}

.filter-form :deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
  padding-right: 8px;
}

.filter-form :deep(.el-select) {
  width: 150px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  border: 1px solid #ebeef5;
}

.stat-card.warning {
  border-left: 4px solid #e6a23c;
}

.stat-card.success {
  border-left: 4px solid #67c23a;
}

.stat-card.danger {
  border-left: 4px solid #f56c6c;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  margin-top: 6px;
  color: #909399;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.table-card {
  overflow: hidden;
}

.pending-panel {
  min-height: 220px;
}

.panel-header {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pending-item {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px;
}

.pending-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.pending-meta {
  color: #606266;
  font-size: 13px;
  margin-bottom: 4px;
}

.pending-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
