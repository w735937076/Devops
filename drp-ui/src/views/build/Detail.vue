<template>
  <div class="page-container">
    <el-page-header @back="() => $router.back()" content="构建详情" />

    <div v-if="build" class="build-detail">
      <!-- 基本信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>构建 #{{ build.buildNumber }}</span>
            <el-tag :type="BuildStatusMap[build.status]?.type">
              {{ BuildStatusMap[build.status]?.label }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="项目">{{ build.projectName }}</el-descriptions-item>
          <el-descriptions-item label="分支">
            <el-tag size="small">{{ build.branch }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Commit">
            <code>{{ build.commitId?.substring(0, 7) }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="触发类型">{{ build.triggerType }}</el-descriptions-item>
          <el-descriptions-item label="触发人">{{ build.triggerUser }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ formatDuration(build.duration) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ build.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ build.endTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="流水线">{{ build.pipelineName || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="build.commitMessage" style="margin-top: 16px;">
          <div style="font-size: 12px; color: #909399; margin-bottom: 4px;">Commit 描述</div>
          <div style="color: #606266;">{{ build.commitMessage }}</div>
        </div>
        <div v-if="build.errorMessage" style="margin-top: 16px;">
          <div style="font-size: 12px; color: #f56c6c; margin-bottom: 4px;">错误信息</div>
          <div style="color: #f56c6c;">{{ build.errorMessage }}</div>
        </div>
      </el-card>

      <!-- 构建日志卡片 -->
      <el-card class="log-card">
        <template #header>
          <div class="card-header">
            <span>构建日志</span>
            <div>
              <el-button size="small" @click="downloadLog" :disabled="!logContent">
                <el-icon><Download /></el-icon> 下载日志
              </el-button>
              <el-button size="small" @click="loadLog">刷新</el-button>
              <el-button size="small" @click="toggleAutoScroll">
                {{ autoScroll ? '取消自动滚动' : '自动滚动' }}
              </el-button>
            </div>
          </div>
        </template>
        <div class="log-container" ref="logContainer">
          <pre class="log-content">{{ logContent || '暂无日志...' }}</pre>
        </div>
      </el-card>

      <!-- 产物卡片 -->
      <el-card class="artifact-card">
        <template #header>
          <div class="card-header">
            <span>构建产物</span>
            <el-tag v-if="build.artifacts?.length" size="small" type="success">
              {{ build.artifacts.length }} 个文件
            </el-tag>
          </div>
        </template>
        <template v-if="build.artifacts?.length">
          <el-table :data="build.artifacts">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="path" label="路径" width="300" show-overflow-tooltip />
            <el-table-column label="大小" width="120">
              <template #default="{ row }">
                {{ formatSize(row.size) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link @click="downloadArtifact(row)">下载</el-button>
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

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button v-if="build.status === 0 || build.status === 1" type="warning" @click="handleCancel">
          取消构建
        </el-button>
        <el-button v-if="build.status === 3" type="primary" @click="handleRetry">
          重新构建
        </el-button>
        <el-button v-if="build.status === 2" type="success" @click="handleDeploy">
          部署
        </el-button>
      </div>
    </div>

    <div v-else v-loading="!build" style="margin-top: 20px;">
      <el-empty description="加载中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { Download } from '@element-plus/icons-vue'
import { getBuildDetail, getBuildLog, getBuildLogWsUrl, cancelBuild, BuildStatusMap, BuildStatus } from '@/api/build'
import type { Build } from '@/api/build'

const route = useRoute()
const router = useRouter()
const buildId = Number(route.params.id)

const build = ref<Build | null>(null)
const logContent = ref('')
const logContainer = ref<HTMLElement | null>(null)
const autoScroll = ref(true)

let ws: WebSocket | null = null
let wsBuffer: string[] = []
let logLoaded = false

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

  const wsUrl = getBuildLogWsUrl(buildId)
  ws = new WebSocket(wsUrl)

  ws.onmessage = (event) => {
    if (!logLoaded) {
      wsBuffer.push(event.data)
      return
    }
    logContent.value += event.data
    if (autoScroll.value) {
      nextTick(() => scrollToBottom())
    }
  }

  ws.onerror = () => {
    console.error('WebSocket 连接失败')
  }

  ws.onclose = () => {
    console.log('WebSocket 连接关闭')
  }
}

function disconnectWebSocket() {
  ws?.close()
  ws = null
}

function scrollToBottom() {
  if (logContainer.value) {
    logContainer.value.scrollTop = logContainer.value.scrollHeight
  }
}

async function loadBuildDetail() {
  try {
    build.value = await getBuildDetail(buildId)
  } catch (error) {
    ElMessage.error('加载构建详情失败')
  }
}

async function loadLog() {
  try {
    const log = await getBuildLog(buildId)
    logContent.value = log || ''

    logLoaded = true
    if (wsBuffer.length > 0) {
      logContent.value += wsBuffer.join('')
      wsBuffer = []
    }

    if (autoScroll.value) {
      nextTick(() => scrollToBottom())
    }
  } catch (error) {
    logLoaded = true
    console.error('加载日志失败', error)
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

async function downloadArtifact(artifact: any) {
  if (!artifact.downloadUrl) {
    ElMessage.warning('下载链接不存在')
    return
  }
  try {
    const token = localStorage.getItem('accessToken')
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
  } catch (error) {
    ElMessage.error('下载失败')
  }
}
</script>

<style scoped>
.build-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-card {
  height: 450px;
  display: flex;
  flex-direction: column;
}

.log-container {
  flex: 1;
  overflow-y: auto;
  background: #1e1e1e;
  padding: 12px;
  border-radius: 4px;
}

.log-content {
  margin: 0;
  color: #d4d4d4;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 16px;
}
</style>
