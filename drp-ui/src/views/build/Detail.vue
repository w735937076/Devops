<template>
  <div class="page-container">
    <el-page-header @back="() => $router.back()" content="构建详情" />

    <div v-if="build" class="build-detail">
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
        <el-button type="primary" @click="openLogDialog">
          <el-icon><Document /></el-icon> 查看日志
        </el-button>
        <el-button v-if="build.status === 0 || build.status === 1" type="warning" @click="handleCancel">
          <el-icon><VideoPause /></el-icon> 取消构建
        </el-button>
        <el-button v-if="build.status === 2" type="success" @click="handleDeploy">
          <el-icon><Upload /></el-icon> 部署
        </el-button>
        <el-button v-if="build.status === 3 || build.status === 4" type="danger" @click="handleRetry">
          <el-icon><RefreshRight /></el-icon> 重试构建
        </el-button>
      </div>

      <!-- 基本信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">
              <el-icon><InfoFilled /></el-icon> 构建信息
            </div>
            <el-tag :type="BuildStatusMap[build.status]?.type" size="large">
              {{ BuildStatusMap[build.status]?.label }}
            </el-tag>
          </div>
        </template>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">项目</span>
            <span class="info-value">{{ build.projectName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">版本</span>
            <span class="info-value version">{{ build.version }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">分支</span>
            <span class="info-value">
              <el-tag size="small" type="primary">
                <el-icon><Link /></el-icon> {{ build.branch }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">状态</span>
            <span class="info-value">
              <el-tag :type="BuildStatusMap[build.status]?.type">
                {{ BuildStatusMap[build.status]?.label }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">Commit ID</span>
            <code class="info-code">{{ build.commitId?.substring(0, 7) }}</code>
          </div>
          <div class="info-item">
            <span class="info-label">触发方式</span>
            <span class="info-value trigger-type">
              <el-icon><Pointer /></el-icon> {{ getTriggerTypeLabel(build.triggerType) }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">触发人</span>
            <span class="info-value">{{ build.triggerUser }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">耗时</span>
            <span class="info-value">{{ formatDuration(build.duration) }}</span>
          </div>
        </div>
        <div v-if="build.commitMessage" class="commit-message">
          <div class="info-label">Commit 描述</div>
          <div class="commit-text">{{ build.commitMessage }}</div>
        </div>
        <div v-if="build.errorMessage" class="error-message">
          <div class="info-label">错误信息</div>
          <div class="error-text">{{ build.errorMessage }}</div>
        </div>
      </el-card>

      <!-- 流水线阶段状态 -->
      <el-card v-if="build.stages?.length" class="stages-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">
              <el-icon><List /></el-icon> 流水线阶段
            </div>
            <div class="stages-summary">
              <span class="summary-item success">
                <el-icon><CircleCheck /></el-icon>
                {{ build.stages.filter(s => s.status === 'SUCCESS').length }} 成功
              </span>
              <span v-if="build.stages.some(s => s.status === 'FAIL')" class="summary-item fail">
                <el-icon><CircleClose /></el-icon>
                {{ build.stages.filter(s => s.status === 'FAIL').length }} 失败
              </span>
              <span v-if="build.stages.some(s => s.status === 'RUNNING')" class="summary-item running">
                <el-icon><Loading /></el-icon>
                进行中
              </span>
            </div>
          </div>
        </template>
        <div class="pipeline-timeline">
          <div v-for="(stage, index) in build.stages" :key="index" class="timeline-step">
            <!-- 连接线 -->
            <div v-if="index > 0" class="step-line" :class="getLineClass(build.stages[index - 1])"></div>
            <!-- 阶段节点 -->
            <div class="step-node-wrapper">
              <div class="step-circle" :class="getStageCircleClass(stage)">
                <el-icon v-if="stage.status === 'SUCCESS'" class="step-icon"><CircleCheck /></el-icon>
                <el-icon v-else-if="stage.status === 'FAIL'" class="step-icon"><CircleClose /></el-icon>
                <el-icon v-else-if="stage.status === 'RUNNING'" class="step-icon spin-icon"><Loading /></el-icon>
                <el-icon v-else-if="stage.status === 'SKIP'" class="step-icon"><Remove /></el-icon>
                <span v-else class="step-index">{{ index + 1 }}</span>
              </div>
              <div class="step-label">
                <div class="step-name">{{ stage.name }}</div>
                <div class="step-meta">
                  <span class="step-status-text" :class="getStageTextClass(stage)">{{ getStageStatusText(stage) }}</span>
                  <span v-if="stage.duration" class="step-duration">
                    <el-icon><Timer /></el-icon>{{ stage.duration }}s
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 构建产物 -->
      <el-card class="artifact-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">
              <el-icon><Folder /></el-icon> 构建产物
            </div>
            <el-button v-if="build.artifacts?.length" type="primary" size="small" @click="downloadAllArtifacts">
              <el-icon><Download /></el-icon> 下载全部
            </el-button>
          </div>
        </template>
        <template v-if="build.artifacts?.length">
          <el-table :data="build.artifacts">
            <el-table-column prop="name" label="产物名称" />
            <el-table-column prop="path" label="存储路径" width="300" show-overflow-tooltip />
            <el-table-column prop="size" label="大小" width="120">
              <template #default="{ row }">
                {{ formatSize(row.size) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="downloadArtifact(row)">
                  <el-icon><Download /></el-icon> 下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <el-empty v-else :image-size="60" description="">
          <template #description>
            <span style="color: #909399; font-size: 13px;">
              <template v-if="build.status === 2">本次构建未生成产物</template>
              <template v-else-if="build.status === 0 || build.status === 1">构建进行中，产物将在构建成功后展示</template>
              <template v-else>构建未成功，无产物</template>
            </span>
          </template>
        </el-empty>
      </el-card>

      <!-- 构建参数 -->
      <el-card v-if="hasBuildParams" class="params-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">
              <el-icon><Setting /></el-icon> 构建参数
            </div>
          </div>
        </template>
        <div class="params-container">
          <div v-for="(value, key) in build.buildParams" :key="key" class="param-item">
            <span class="param-key">{{ key }}</span>
            <span class="param-value">{{ value }}</span>
          </div>
        </div>
      </el-card>

    </div>

    <div v-else v-loading="!build" style="margin-top: 20px;">
      <el-empty description="加载中..." />
    </div>

    <!-- 日志弹框 -->
    <el-dialog
      v-model="logDialogVisible"
      width="80%"
      top="4vh"
      :destroy-on-close="false"
      class="log-dialog"
      @opened="onLogDialogOpened"
    >
      <template #header>
        <div class="log-dialog-header">
          <div class="log-dialog-title">
            <el-icon><Document /></el-icon>
            <span>构建日志</span>
            <el-tag v-if="build" :type="BuildStatusMap[build.status]?.type" size="small">
              {{ BuildStatusMap[build.status]?.label }}
            </el-tag>
            <span v-if="isStreaming" class="live-badge">
              <span class="live-dot"></span> 实时
            </span>
          </div>
          <div class="log-dialog-actions">
            <el-tooltip content="自动滚动到底部">
              <el-button
                size="small"
                :type="autoScroll ? 'primary' : 'default'"
                @click="toggleAutoScroll"
              >
                <el-icon><Bottom /></el-icon>
                {{ autoScroll ? '自动滚动 开' : '自动滚动 关' }}
              </el-button>
            </el-tooltip>
            <el-button size="small" @click="loadLog" :loading="logLoading">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
            <el-button size="small" @click="downloadLog" :disabled="!logContent">
              <el-icon><Download /></el-icon> 下载
            </el-button>
          </div>
        </div>
      </template>
      <div class="log-dialog-body" ref="logDialogContainer">
        <div v-if="!logContent && logLoading" class="log-loading">
          <el-icon class="spin-icon"><Loading /></el-icon>
          <span>日志加载中...</span>
        </div>
        <pre v-else class="log-content">{{ logContent || '暂无日志' }}</pre>
      </div>
      <template #footer>
        <div class="log-dialog-footer">
          <span class="log-lines">共 {{ logLineCount }} 行</span>
          <el-button @click="logDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import {
  Download,
  ArrowLeft,
  Document,
  VideoPause,
  Upload,
  RefreshRight,
  InfoFilled,
  Link,
  Pointer,
  List,
  CircleCheck,
  CircleClose,
  Loading,
  ArrowRight,
  Folder,
  Setting,
  Refresh,
  Timer,
  Remove,
  Bottom
} from '@element-plus/icons-vue'
import { getBuildDetail, getBuildLog, getBuildLogWsUrl, cancelBuild, BuildStatusMap, BuildStatus } from '@/api/build'

interface StageDetail {
  name: string
  type: string
  enabled: boolean
  duration?: number
  status?: string
}

interface BuildDetail extends Record<string, any> {
  id: number
  buildNumber: number
  projectId: number
  projectName: string
  pipelineId: number
  pipelineName: string
  branch: string
  commitId: string
  commitMessage: string
  status: BuildStatus
  statusDesc: string
  triggerType: string
  triggerUser: string
  duration: number
  startTime: string
  endTime: string
  errorMessage: string
  artifacts: Artifact[]
  createTime: string
  version?: string
  stages?: StageDetail[]
  buildParams?: Record<string, string>
}

interface Artifact {
  name: string
  path: string
  size: number
  downloadUrl: string
  createTime?: string
}

const route = useRoute()
const router = useRouter()
const buildId = Number(route.params.id)

const build = ref<BuildDetail | null>(null)
const logContent = ref('')
const logDialogContainer = ref<HTMLElement | null>(null)
const logDialogVisible = ref(false)
const logLoading = ref(false)
const autoScroll = ref(true)
const isStreaming = ref(false)

let ws: WebSocket | null = null
let wsBuffer: string[] = []
let logLoaded = false

const hasBuildParams = computed(() => {
  return build.value?.buildParams && Object.keys(build.value.buildParams).length > 0
})

const logLineCount = computed(() => {
  if (!logContent.value) return 0
  return logContent.value.split('\n').length
})

function openLogDialog() {
  logDialogVisible.value = true
}

function onLogDialogOpened() {
  if (autoScroll.value) {
    scrollDialogToBottom()
  }
}

function scrollDialogToBottom() {
  nextTick(() => {
    if (logDialogContainer.value) {
      logDialogContainer.value.scrollTop = logDialogContainer.value.scrollHeight
    }
  })
}

watch(logContent, () => {
  if (logDialogVisible.value && autoScroll.value) {
    scrollDialogToBottom()
  }
})

onMounted(async () => {
  loadBuildDetail()
  await loadLog()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

function connectWebSocket() {
  const isRunning = build.value && (build.value.status === BuildStatus.PENDING || build.value.status === BuildStatus.RUNNING)
  if (!isRunning) return

  isStreaming.value = true
  const wsUrl = getBuildLogWsUrl(buildId)
  ws = new WebSocket(wsUrl)

  ws.onmessage = (event) => {
    if (!logLoaded) {
      wsBuffer.push(event.data)
      return
    }
    logContent.value += event.data
  }

  ws.onerror = () => {
    isStreaming.value = false
    console.error('WebSocket 连接失败')
  }

  ws.onclose = () => {
    isStreaming.value = false
    console.log('WebSocket 连接关闭')
  }
}

function disconnectWebSocket() {
  ws?.close()
  ws = null
  isStreaming.value = false
}

async function loadBuildDetail() {
  try {
    build.value = await getBuildDetail(buildId)
  } catch (error) {
    ElMessage.error('加载构建详情失败')
  }
}

async function loadLog() {
  logLoading.value = true
  try {
    const log = await getBuildLog(buildId)
    logContent.value = log || ''

    logLoaded = true
    if (wsBuffer.length > 0) {
      logContent.value += wsBuffer.join('')
      wsBuffer = []
    }
  } catch (error) {
    logLoaded = true
    console.error('加载日志失败', error)
  } finally {
    logLoading.value = false
  }
}

function toggleAutoScroll() {
  autoScroll.value = !autoScroll.value
}

function downloadLog() {
  if (!logContent.value) return
  const blob = new Blob([logContent.value], { type: 'text/plain;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `build-${build.value?.buildNumber || buildId}.log`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

async function handleCancel() {
  try {
    await cancelBuild(buildId)
    ElMessage.success('构建已取消')
    loadBuildDetail()
  } catch (error) {
    ElMessage.error('取消构建失败')
  }
}

function handleRetry() {
  router.push('/builds')
}

function handleDeploy() {
  ElMessage.info('部署功能开发中')
}

function getTriggerTypeLabel(type: string): string {
  const map: Record<string, string> = {
    'MANUAL': '手动',
    'PUSH': '代码推送',
    'TAG': '标签发布',
    'SCHEDULE': '定时触发',
    'API': 'API触发'
  }
  return map[type] || type
}

function getStageClass(stage: StageDetail): string {
  const status = stage.status || 'pending'
  return `stage-${status}`
}

function getStageCircleClass(stage: StageDetail): string {
  const status = stage.status
  if (status === 'SUCCESS') return 'circle-success'
  if (status === 'FAIL') return 'circle-fail'
  if (status === 'RUNNING') return 'circle-running'
  if (status === 'SKIP') return 'circle-skip'
  return 'circle-pending'
}

function getStageTextClass(stage: StageDetail): string {
  const status = stage.status
  if (status === 'SUCCESS') return 'text-success'
  if (status === 'FAIL') return 'text-fail'
  if (status === 'RUNNING') return 'text-running'
  if (status === 'SKIP') return 'text-skip'
  return 'text-pending'
}

function getLineClass(stage: StageDetail): string {
  const status = stage.status
  if (status === 'SUCCESS') return 'line-success'
  if (status === 'FAIL') return 'line-fail'
  if (status === 'RUNNING') return 'line-running'
  return 'line-pending'
}

function getStageStatusIcon(stage: StageDetail): string {
  const status = stage.status
  if (status === 'SUCCESS') return 'check'
  if (status === 'FAIL') return 'fail'
  if (status === 'RUNNING') return 'loading'
  return ''
}

function getStageStatusText(stage: StageDetail): string {
  const status = stage.status
  if (status === 'SUCCESS') return '成功'
  if (status === 'FAIL') return '失败'
  if (status === 'RUNNING') return '进行中'
  if (status === 'SKIP') return '跳过'
  return '等待'
}

function formatDuration(seconds: number): string {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}分${secs}秒` : `${secs}秒`
}

function formatSize(bytes: number): string {
  if (!bytes) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024
    i++
  }
  return `${bytes.toFixed(2)} ${units[i]}`
}

async function downloadArtifact(artifact: Artifact) {
  if (!artifact.downloadUrl) {
    ElMessage.warning('下载链接不存在')
    return
  }
  try {
    const token = localStorage.getItem('accessToken')
    console.log('下载产物:', artifact.name, 'URL:', artifact.downloadUrl)
    const response = await axios.get(artifact.downloadUrl, {
      responseType: 'blob',
      headers: { Authorization: `Bearer ${token}` }
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = artifact.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error: any) {
    console.error('下载失败:', error)
    const message = error?.response?.data?.message || error?.message || '下载失败'
    ElMessage.error(message)
  }
}

function downloadAllArtifacts() {
  if (!build.value?.artifacts?.length) return
  build.value.artifacts.forEach(artifact => {
    downloadArtifact(artifact)
  })
}
</script>

<style scoped>
.build-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.info-card .el-card__header {
  padding: 12px 16px;
  background: #f5f7fa;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.info-value.version {
  color: var(--el-color-primary);
  font-weight: 600;
}

.info-code {
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.trigger-type {
  color: #67c23a;
}

.commit-message {
  padding: 0 16px 16px;
}

.commit-text {
  color: #606266;
  line-height: 1.6;
  margin-top: 4px;
}

.error-message {
  padding: 0 16px 16px;
}

.error-text {
  color: #f56c6c;
  line-height: 1.6;
  margin-top: 4px;
}

/* ===== 流水线阶段 ===== */
.stages-summary {
  display: flex;
  gap: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
}

.summary-item.success {
  color: #67c23a;
}

.summary-item.fail {
  color: #f56c6c;
}

.summary-item.running {
  color: #409eff;
}

.pipeline-timeline {
  display: flex;
  align-items: flex-start;
  overflow-x: auto;
  padding: 12px 0 8px;
  gap: 0;
}

.timeline-step {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.step-line {
  width: 48px;
  height: 2px;
  background: #dcdfe6;
  flex-shrink: 0;
  transition: background 0.3s;
}

.step-line.line-success {
  background: #67c23a;
}

.step-line.line-fail {
  background: #f56c6c;
}

.step-line.line-running {
  background: linear-gradient(to right, #409eff, #dcdfe6);
  background-size: 200% 100%;
  animation: line-flow 1.5s linear infinite;
}

@keyframes line-flow {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}

.step-node-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 80px;
}

.step-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  border: 2px solid #dcdfe6;
  background: #f5f7fa;
  transition: all 0.3s;
  position: relative;
}

.step-circle.circle-success {
  background: #f0f9eb;
  border-color: #67c23a;
  color: #67c23a;
}

.step-circle.circle-fail {
  background: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
}

.step-circle.circle-running {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15);
  animation: ring-pulse 1.5s ease-in-out infinite;
}

.step-circle.circle-skip {
  background: #f4f4f5;
  border-color: #c0c4cc;
  color: #c0c4cc;
}

.step-circle.circle-pending {
  background: #f5f7fa;
  border-color: #dcdfe6;
  color: #909399;
}

@keyframes ring-pulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15); }
  50% { box-shadow: 0 0 0 8px rgba(64, 158, 255, 0.08); }
}

.step-icon {
  font-size: 18px;
}

.step-index {
  font-size: 14px;
  font-weight: 600;
  color: #909399;
}

.spin-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.step-label {
  text-align: center;
}

.step-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
}

.step-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 2px;
}

.step-status-text {
  font-size: 12px;
}

.step-status-text.text-success { color: #67c23a; }
.step-status-text.text-fail { color: #f56c6c; }
.step-status-text.text-running { color: #409eff; }
.step-status-text.text-skip { color: #c0c4cc; }
.step-status-text.text-pending { color: #909399; }

.step-duration {
  font-size: 11px;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  gap: 2px;
}

/* ===== 构建参数 ===== */
.params-card .el-card__body {
  padding: 0;
}

.params-container {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-family: 'Consolas', monospace;
  font-size: 13px;
}

.param-item {
  display: flex;
  margin-bottom: 8px;
}

.param-item:last-child {
  margin-bottom: 0;
}

.param-key {
  color: #909399;
  min-width: 150px;
}

.param-value {
  color: #67c23a;
}

/* ===== 日志弹框 ===== */
.log-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.log-dialog-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
}

.log-dialog-actions {
  display: flex;
  gap: 8px;
}

.live-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
  border-radius: 10px;
  padding: 2px 8px;
}

.live-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f56c6c;
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.2; }
}

.log-dialog-body {
  height: 60vh;
  overflow-y: auto;
  background: #1a1a2e;
  border-radius: 6px;
  padding: 16px;
  font-family: 'Consolas', 'Courier New', monospace;
  scrollbar-width: thin;
  scrollbar-color: #444 #1a1a2e;
}

.log-dialog-body::-webkit-scrollbar {
  width: 6px;
}

.log-dialog-body::-webkit-scrollbar-track {
  background: #1a1a2e;
}

.log-dialog-body::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 3px;
}

.log-content {
  margin: 0;
  color: #c8d3f0;
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.log-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 100%;
  color: #606266;
  font-size: 14px;
}

.log-dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.log-lines {
  font-size: 13px;
  color: #909399;
}
</style>

<style>
.log-dialog .el-dialog__header {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.log-dialog .el-dialog__body {
  padding: 16px 20px;
  background: #13131f;
}

.log-dialog .el-dialog__footer {
  padding: 12px 20px;
  border-top: 1px solid #2a2a3e;
  background: #13131f;
}

.log-dialog .log-dialog-footer .el-button {
  color: #909399;
  border-color: #444;
  background: #1e1e30;
}

.log-dialog .log-dialog-footer .log-lines {
  color: #606680;
}
</style>
