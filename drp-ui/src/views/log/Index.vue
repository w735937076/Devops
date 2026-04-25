<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">日志中心</h2>
    </div>

    <!-- Tab 导航 -->
    <div class="log-tabs">
      <div
        class="log-tab"
        :class="{ active: activeTab === 'operation' }"
        @click="activeTab = 'operation'"
      >
        <i class="fas fa-history"></i> 操作日志
      </div>
      <div
        class="log-tab"
        :class="{ active: activeTab === 'build' }"
        @click="activeTab = 'build'"
      >
        <i class="fas fa-hammer"></i> 构建日志
      </div>
      <div
        class="log-tab"
        :class="{ active: activeTab === 'realtime' }"
        @click="activeTab = 'realtime'"
      >
        <i class="fas fa-desktop"></i> 实时运行日志
      </div>
    </div>

    <!-- 操作日志 Tab -->
    <div v-show="activeTab === 'operation'" class="tab-content">
      <div class="card-container">
        <div class="card-header">
          <div class="card-title">
            <i class="fas fa-history"></i> 操作日志
          </div>
          <el-button type="success" @click="handleExport">
            <i class="fas fa-download"></i> 导出
          </el-button>
        </div>

        <!-- 筛选工具栏 -->
        <div class="filter-toolbar">
          <el-select
            v-model="logQuery.operationType"
            placeholder="操作类型"
            clearable
            size="small"
            style="width: 120px"
          >
            <el-option label="构建" value="BUILD" />
            <el-option label="部署" value="DEPLOY" />
            <el-option label="回滚" value="ROLLBACK" />
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
          </el-select>

          <el-select
            v-model="logQuery.projectId"
            placeholder="选择项目"
            clearable
            size="small"
            style="width: 160px"
          >
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>

          <el-date-picker
            v-model="logQuery.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="small"
            style="width: 240px"
            @change="handleDateRangeChange"
          />

          <el-input
            v-model="logQuery.keyword"
            placeholder="搜索关键字"
            clearable
            size="small"
            style="width: 180px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>

          <el-button type="primary" size="small" @click="handleSearch">
            <i class="fas fa-search"></i> 查询
          </el-button>
          <el-button size="small" @click="handleReset">
            <i class="fas fa-refresh"></i> 重置
          </el-button>
        </div>

        <!-- 日志表格 -->
        <el-table
          :data="operationLogs"
          style="width: 100%"
          v-loading="loading"
          @row-click="handleRowClick"
          row-class-name="clickable-row"
        >
          <el-table-column prop="createTime" label="时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="operationType" label="操作类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getOperationTypeTagType(row.operationType)" size="small">
                {{ getOperationTypeText(row.operationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="projectName" label="项目" width="140" />
          <el-table-column prop="env" label="环境" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.env" class="env-tag">{{ row.env }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本" width="120" />
          <el-table-column prop="detail" label="详情" min-width="200">
            <template #default="{ row }">
              <span class="detail-text" @click.stop="showLogDetail(row)">
                {{ row.detail }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="耗时" width="90" align="right">
            <template #default="{ row }">
              <span v-if="row.duration" class="duration-text">
                {{ row.duration }}ms
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <i
                class="fas fa-external-link-alt jump-icon"
                title="跳转到详情"
                @click.stop="jumpToDetail(row)"
              ></i>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="logQuery.page"
            v-model:page-size="logQuery.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadOperationLogs"
            @current-change="loadOperationLogs"
          />
        </div>
      </div>
    </div>

    <!-- 构建日志 Tab -->
    <div v-show="activeTab === 'build'" class="tab-content">
      <div class="card-container">
        <div class="card-header">
          <div class="card-title">
            <i class="fas fa-hammer"></i> 构建日志
          </div>
          <div class="header-actions">
            <el-select
              v-model="buildLogProjectId"
              placeholder="选择项目"
              clearable
              size="small"
              style="width: 160px"
              @change="loadBuildList"
            >
              <el-option
                v-for="project in projectList"
                :key="project.id"
                :label="project.name"
                :value="project.id"
              />
            </el-select>

            <el-select
              v-model="buildLogBuildId"
              placeholder="选择构建"
              clearable
              size="small"
              style="width: 200px"
              :disabled="!buildLogProjectId"
              @change="loadBuildLog"
            >
              <el-option
                v-for="build in buildList"
                :key="build.id"
                :label="`#${build.buildNumber} - ${build.branch}`"
                :value="build.id"
              />
            </el-select>

            <el-input
              v-model="buildLogKeyword"
              placeholder="搜索关键字"
              clearable
              size="small"
              style="width: 160px"
            >
              <template #prefix>
                <i class="fas fa-search"></i>
              </template>
            </el-input>

            <el-button type="primary" size="small" @click="loadBuildLog" :disabled="!buildLogBuildId">
              <i class="fas fa-search"></i> 查询
            </el-button>
          </div>
        </div>

        <!-- 日志级别过滤 -->
        <div class="log-level-toolbar">
          <span class="toolbar-label">日志级别：</span>
          <el-checkbox-group v-model="logLevelFilters" size="small">
            <el-checkbox label="error">
              <span class="log-level-error">■ ERROR</span>
            </el-checkbox>
            <el-checkbox label="warn">
              <span class="log-level-warn">■ WARN</span>
            </el-checkbox>
            <el-checkbox label="info">
              <span class="log-level-info">■ INFO</span>
            </el-checkbox>
          </el-checkbox-group>

          <span v-if="highlightKeyword" class="highlight-info">
            高亮关键字：<span class="highlight-tag">{{ highlightKeyword }}</span>
          </span>
        </div>

        <!-- 构建日志内容 -->
        <div class="build-log-terminal" ref="buildLogContainer">
          <div
            v-for="(line, index) in filteredBuildLogLines"
            :key="index"
            class="log-line"
            :class="getLogLineClass(line)"
            v-html="highlightLogLine(line)"
          ></div>
          <div v-if="!buildLogContent" class="empty-log">
            <i class="fas fa-file-alt"></i>
            <p>请选择一个构建查看日志</p>
          </div>
        </div>

        <!-- 日志底部信息 -->
        <div class="log-footer">
          <span>
            <i class="fas fa-info-circle"></i>
            共 {{ buildLogLines.length }} 行日志
          </span>
          <span v-if="lastUpdateTime">
            最后更新: {{ lastUpdateTime }}
          </span>
          <span class="download-link" @click="downloadBuildLog">
            <i class="fas fa-download"></i> 下载完整日志
          </span>
        </div>
      </div>
    </div>

    <!-- 实时运行日志 Tab -->
    <div v-show="activeTab === 'realtime'" class="tab-content">
      <div class="grid-2">
        <div class="card-container">
          <div class="card-header">
            <div class="card-title">
              <i class="fas fa-desktop"></i> 实时运行日志
            </div>
            <div class="header-actions">
              <el-select
                v-model="realtimeServerId"
                placeholder="选择服务器"
                clearable
                size="small"
                style="width: 160px"
              >
                <el-option
                  v-for="server in serverList"
                  :key="server.id"
                  :label="`${server.name} (${server.hostname})`"
                  :value="server.id"
                />
              </el-select>

              <el-select
                v-model="realtimeLogPath"
                placeholder="日志路径"
                clearable
                size="small"
                style="width: 200px"
                :disabled="!realtimeServerId"
              >
                <el-option
                  v-for="path in logPathOptions"
                  :key="path"
                  :label="path"
                  :value="path"
                />
              </el-select>
            </div>
          </div>

          <!-- 日志级别过滤 -->
          <div class="log-level-toolbar">
            <span class="toolbar-label">显示级别：</span>
            <el-checkbox-group v-model="realtimeLogLevels" size="small">
              <el-checkbox label="error">
                <span class="log-level-error">ERROR</span>
              </el-checkbox>
              <el-checkbox label="warn">
                <span class="log-level-warn">WARN</span>
              </el-checkbox>
              <el-checkbox label="info">
                <span class="log-level-info">INFO</span>
              </el-checkbox>
            </el-checkbox-group>

            <el-input
              v-model="realtimeKeyword"
              placeholder="过滤关键字"
              clearable
              size="small"
              style="width: 140px"
            >
              <template #prefix>
                <i class="fas fa-filter"></i>
              </template>
            </el-input>

            <div class="toolbar-actions">
              <el-button
                type="primary"
                size="small"
                :disabled="!realtimeServerId || !realtimeLogPath || wsConnected"
                @click="connectWebSocket"
              >
                <i class="fas fa-play"></i> 连接
              </el-button>
              <el-button
                type="danger"
                size="small"
                :disabled="!wsConnected"
                @click="disconnectWebSocket"
              >
                <i class="fas fa-stop"></i> 断开
              </el-button>
              <el-button size="small" @click="clearRealtimeLog">
                <i class="fas fa-trash-alt"></i> 清屏
              </el-button>
            </div>
          </div>

          <!-- 实时日志终端 -->
          <div class="realtime-terminal" ref="realtimeTerminal">
            <div
              v-for="(line, index) in filteredRealtimeLogs"
              :key="index"
              class="log-line"
              :class="getLogLineClass(line)"
              v-html="highlightLogLine(line)"
            ></div>
          </div>

          <!-- 连接状态 -->
          <div class="connection-status">
            <span :class="wsConnected ? 'status-online' : 'status-offline'">
              <i :class="wsConnected ? 'fas fa-circle' : 'far fa-circle'"></i>
              {{ wsConnected ? '已连接' : '未连接' }}
            </span>
            <span v-if="wsConnected && currentServer">
              {{ currentServer.hostname }}
            </span>
            <span v-if="wsConnected">
              {{ realtimeLogPath }} | 接收: {{ realtimeLogs.length }} 行
            </span>
          </div>
        </div>

        <!-- 服务器状态 -->
        <div class="card-container">
          <div class="card-header">
            <div class="card-title">
              <i class="fas fa-server"></i> 服务器状态
            </div>
          </div>

          <el-table :data="serverList" style="width: 100%">
            <el-table-column prop="name" label="服务器" min-width="160">
              <template #default="{ row }">
                <div class="server-name-cell">
                  <span class="server-status-dot" :class="{ online: row.online }"></span>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="hostname" label="IP地址" width="120" />
            <el-table-column prop="app" label="运行应用" min-width="120" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.online ? 'success' : 'info'" size="small">
                  {{ row.online ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  :disabled="!row.online"
                  @click="quickConnect(row)"
                >
                  连接
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="操作时间">
          {{ formatDateTime(currentLog.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ currentLog.operator }}
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTagType(currentLog.operationType)" size="small">
            {{ getOperationTypeText(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ currentLog.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="项目">
          {{ currentLog.projectName }}
        </el-descriptions-item>
        <el-descriptions-item label="环境">
          <span v-if="currentLog.env" class="env-tag">{{ currentLog.env }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="版本">
          {{ currentLog.version || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ currentLog.duration ? currentLog.duration + 'ms' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          {{ currentLog.ip || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentLog.errorMessage" :span="2">
          <span class="error-message">{{ currentLog.errorMessage }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="详情" :span="2">
          <pre class="detail-pre">{{ formatDetail(currentLog.detail) }}</pre>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentLog.operationType === 'BUILD'"
          type="primary"
          @click="jumpToBuildDetail"
        >
          <i class="fas fa-external-link-alt"></i> 跳转到构建详情
        </el-button>
        <el-button
          v-if="currentLog.operationType === 'DEPLOY'"
          type="primary"
          @click="jumpToDeployDetail"
        >
          <i class="fas fa-external-link-alt"></i> 跳转到部署详情
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getOperationLogs,
  getOperationLogDetail,
  exportOperationLogs,
  getBuildLogs,
  getAppLogWsUrl,
  OperationType,
  OperationTypeMap,
  type OperationLog,
  type LogQuery,
  type ServerStatus
} from '@/api/log'
import { getProjectPage } from '@/api/project'
import { getBuildList } from '@/api/build'
import { useRouter } from 'vue-router'

const router = useRouter()

// Tab 状态
const activeTab = ref('operation')

// 操作日志相关
const loading = ref(false)
const operationLogs = ref<OperationLog[]>([])
const total = ref(0)
const logQuery = reactive<LogQuery>({
  operationType: '',
  projectId: undefined,
  dateRange: [],
  keyword: '',
  page: 1,
  size: 10
})
const projectList = ref<any[]>([])

// 构建日志相关
const buildLogProjectId = ref<number | undefined>()
const buildLogBuildId = ref<number | undefined>()
const buildLogKeyword = ref('')
const buildList = ref<any[]>([])
const buildLogContent = ref('')
const buildLogLines = ref<string[]>([])
const buildLogContainer = ref<HTMLElement>()
const lastUpdateTime = ref('')
const logLevelFilters = ref(['error', 'warn', 'info'])
const highlightKeyword = computed(() => buildLogKeyword.value)

// 实时日志相关
const realtimeServerId = ref<number | undefined>()
const realtimeLogPath = ref('')
const realtimeKeyword = ref('')
const realtimeLogLevels = ref(['error', 'warn', 'info'])
const realtimeLogs = ref<string[]>([])
const realtimeTerminal = ref<HTMLElement>()
const wsConnected = ref(false)
const ws = ref<WebSocket | null>(null)
const serverList = ref<ServerStatus[]>([])

const logPathOptions = [
  '/opt/app/logs/app.log',
  '/var/log/nginx/access.log',
  '/var/log/nginx/error.log',
  '/opt/app/logs/error.log'
]

const currentLog = ref<any>({})
const detailVisible = ref(false)

// 计算属性
const filteredBuildLogLines = computed(() => {
  return buildLogLines.value.filter((line) => {
    // 级别过滤
    const isError = line.toLowerCase().includes('error') || line.includes('[ERROR]')
    const isWarn = line.toLowerCase().includes('warn') || line.includes('[WARN]')
    const isInfo = !isError && !isWarn

    if (logLevelFilters.value.includes('error') && isError) return true
    if (logLevelFilters.value.includes('warn') && isWarn) return true
    if (logLevelFilters.value.includes('info') && isInfo) return true

    return false
  })
})

const filteredRealtimeLogs = computed(() => {
  return realtimeLogs.value.filter((line) => {
    // 级别过滤
    const isError = line.toLowerCase().includes('error') || line.includes('[ERROR]')
    const isWarn = line.toLowerCase().includes('warn') || line.includes('[WARN]')
    const isInfo = !isError && !isWarn

    if (logLevelFilters.value.includes('error') && isError) return true
    if (logLevelFilters.value.includes('warn') && isWarn) return true
    if (logLevelFilters.value.includes('info') && isInfo) return true

    return false
  })
})

const currentServer = computed(() => {
  return serverList.value.find((s) => s.id === realtimeServerId.value)
})

// 方法
async function loadProjectList() {
  try {
    const res = await getProjectPage({ page: 1, pageSize: 100 })
    projectList.value = res.records || []
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

async function loadServerList() {
  // 模拟服务器数据，实际应该调用 API
  serverList.value = [
    { id: 1, name: 'Dev Server 1', ip: '10.0.0.1', app: 'drp-api, drp-web', online: true },
    { id: 2, name: 'Dev Server 2', ip: '10.0.0.2', app: 'drp-api', online: true },
    { id: 3, name: 'Prod Server 1', ip: '10.0.1.1', app: 'drp-api, drp-gateway', online: true },
    { id: 4, name: 'Prod Server 2', ip: '10.0.1.2', app: 'drp-web', online: false }
  ]
}

async function loadOperationLogs() {
  loading.value = true
  try {
    const params: LogQuery = {
      page: logQuery.page,
      size: logQuery.size,
      operationType: logQuery.operationType || undefined,
      projectId: logQuery.projectId || undefined,
      keyword: logQuery.keyword || undefined,
      startTime: logQuery.startTime || undefined,
      endTime: logQuery.endTime || undefined
    }
    const res = await getOperationLogs(params)
    operationLogs.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载操作日志失败', error)
    ElMessage.error('加载操作日志失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  logQuery.page = 1
  loadOperationLogs()
}

function handleReset() {
  logQuery.operationType = ''
  logQuery.projectId = undefined
  logQuery.dateRange = []
  logQuery.keyword = ''
  logQuery.page = 1
  loadOperationLogs()
}

function handleDateRangeChange(val: any) {
  if (val && val.length === 2) {
    const formatDate = (date: Date) => {
      const pad = (n: number) => n.toString().padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    }
    logQuery.startTime = formatDate(val[0])
    logQuery.endTime = formatDate(val[1])
  } else {
    logQuery.startTime = undefined
    logQuery.endTime = undefined
  }
}

function handleExport() {
  exportOperationLogs(logQuery)
}

function handleRowClick(row: OperationLog) {
  showLogDetail(row)
}

function showLogDetail(row: OperationLog) {
  currentLog.value = row
  detailVisible.value = true
}

function jumpToDetail(row: OperationLog) {
  showLogDetail(row)
}

function jumpToBuildDetail() {
  detailVisible.value = false
  router.push(`/builds/${currentLog.value.projectId}/detail/${getBuildIdFromDetail()}`)
}

function jumpToDeployDetail() {
  detailVisible.value = false
  router.push(`/deploys/${currentLog.value.projectId}/detail`)
}

function getBuildIdFromDetail() {
  // 从详情中提取构建ID
  const match = currentLog.value.detail?.match(/#(\d+)/)
  return match ? match[1] : null
}

async function loadBuildList() {
  if (!buildLogProjectId.value) {
    buildList.value = []
    return
  }
  try {
    const res = await getBuildList({ projectId: buildLogProjectId.value })
    buildList.value = res.records?.slice(0, 20) || []
  } catch (error) {
    console.error('加载构建列表失败', error)
  }
}

async function loadBuildLog() {
  if (!buildLogBuildId.value) {
    buildLogContent.value = ''
    buildLogLines.value = []
    return
  }

  try {
    const res = await getBuildLogs(buildLogBuildId.value)
    buildLogContent.value = res
    buildLogLines.value = res.split('\n')
    lastUpdateTime.value = new Date().toLocaleString()

    await nextTick()
    if (buildLogContainer.value) {
      buildLogContainer.value.scrollTop = buildLogContainer.value.scrollHeight
    }
  } catch (error) {
    console.error('加载构建日志失败', error)
    ElMessage.error('加载构建日志失败')
  }
}

function downloadBuildLog() {
  if (!buildLogContent.value) return
  const blob = new Blob([buildLogContent.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `build_${buildLogBuildId.value}_log_${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

function getLogLineClass(line: string): string {
  const lowerLine = line.toLowerCase()
  if (lowerLine.includes('error') || line.includes('[ERROR]')) return 'log-error'
  if (lowerLine.includes('warn') || line.includes('[WARN]')) return 'log-warn'
  if (lowerLine.includes('success') || line.includes('[Success]')) return 'log-success'
  return 'log-info'
}

function highlightLogLine(line: string): string {
  if (!buildLogKeyword.value) return escapeHtml(line)

  const keyword = buildLogKeyword.value
  const regex = new RegExp(`(${escapeRegex(keyword)})`, 'gi')
  return escapeHtml(line).replace(regex, '<span class="highlight">$1</span>')
}

function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function connectWebSocket() {
  if (!realtimeServerId.value || !realtimeLogPath.value) {
    ElMessage.warning('请选择服务器和日志路径')
    return
  }

  const wsUrl = getAppLogWsUrl()
  ws.value = new WebSocket(wsUrl)

  ws.value.onopen = () => {
    wsConnected.value = true
    ElMessage.success('WebSocket 连接成功')
    // 发送连接命令
    ws.value?.send(`connect:${realtimeServerId.value}:${realtimeLogPath.value}`)
  }

  ws.value.onmessage = (event) => {
    realtimeLogs.value.push(event.data)
    // 自动滚动
    nextTick(() => {
      if (realtimeTerminal.value) {
        realtimeTerminal.value.scrollTop = realtimeTerminal.value.scrollHeight
      }
    })
  }

  ws.value.onerror = (error) => {
    console.error('WebSocket 错误', error)
    ElMessage.error('WebSocket 连接错误')
  }

  ws.value.onclose = () => {
    wsConnected.value = false
    ElMessage.warning('WebSocket 连接已关闭')
  }
}

function disconnectWebSocket() {
  if (ws.value) {
    ws.value.send('disconnect')
    ws.value.close()
    ws.value = null
  }
}

function clearRealtimeLog() {
  realtimeLogs.value = []
}

function quickConnect(server: ServerStatus) {
  realtimeServerId.value = server.id
  activeTab.value = 'realtime'
}

function getOperationTypeText(type: string): string {
  return OperationTypeMap[type]?.label || type
}

function getOperationTypeTagType(type: string): string {
  return OperationTypeMap[type]?.type || 'info'
}

function formatDateTime(dateTime: string): string {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ').substring(0, 19)
}

function formatDetail(detail: string): string {
  if (!detail) return ''
  try {
    return JSON.stringify(JSON.parse(detail), null, 2)
  } catch {
    return detail
  }
}

// 生命周期
onMounted(() => {
  loadProjectList()
  loadServerList()
  loadOperationLogs()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.log-tabs {
  display: flex;
  background: white;
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.log-tab {
  padding: 12px 24px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.log-tab i {
  font-size: 14px;
}

.log-tab:hover {
  color: #409eff;
}

.log-tab.active {
  background: #409eff;
  color: white;
}

.tab-content {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-container {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: #409eff;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  margin: -16px -16px 16px -16px;
}

.toolbar-label {
  font-size: 13px;
  color: #606266;
}

.toolbar-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.log-level-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.log-level-error {
  color: #f56c6c;
  font-weight: 600;
}

.log-level-warn {
  color: #e6a23c;
  font-weight: 600;
}

.log-level-info {
  color: #409eff;
  font-weight: 600;
}

.highlight-info {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.highlight-tag {
  background: #fef0f0;
  color: #f56c6c;
  padding: 1px 4px;
  border-radius: 2px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0;
}

.clickable-row {
  cursor: pointer;
}

.clickable-row:hover {
  background-color: #f5f7fa;
}

.env-tag {
  background: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.detail-text {
  cursor: pointer;
  color: #409eff;
}

.detail-text:hover {
  text-decoration: underline;
}

.duration-text {
  color: #909399;
}

.jump-icon {
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
}

.jump-icon:hover {
  color: #66b1ff;
}

.build-log-terminal {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  max-height: 500px;
  overflow-y: auto;
  color: #d4d4d4;
}

.log-line {
  margin-bottom: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.log-line .time {
  color: #858585;
}

.log-line.log-error {
  color: #f14c4c;
}

.log-line.log-warn {
  color: #dcdcaa;
}

.log-line.log-success {
  color: #6a9955;
}

.log-line.log-info {
  color: #4ec9b0;
}

.log-line :deep(.highlight) {
  background: #fef0f0;
  color: #f14c4c;
  padding: 0 2px;
  border-radius: 2px;
}

.empty-log {
  text-align: center;
  padding: 60px 20px;
  color: #606266;
}

.empty-log i {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.log-footer {
  padding: 12px 16px;
  background: #1e1e1e;
  color: #858585;
  font-size: 12px;
  border-top: 1px solid #333;
  display: flex;
  gap: 16px;
  align-items: center;
}

.download-link {
  margin-left: auto;
  color: #409eff;
  cursor: pointer;
}

.download-link:hover {
  text-decoration: underline;
}

.realtime-terminal {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  height: 400px;
  overflow-y: auto;
  color: #d4d4d4;
}

.connection-status {
  padding: 12px 16px;
  background: #1e1e1e;
  color: #858585;
  font-size: 12px;
  border-top: 1px solid #333;
  display: flex;
  gap: 16px;
  align-items: center;
}

.status-online {
  color: #67c23a;
}

.status-offline {
  color: #909399;
}

.server-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.server-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
}

.server-status-dot.online {
  background: #67c23a;
}

.error-message {
  color: #f56c6c;
}

.detail-pre {
  max-height: 150px;
  overflow: auto;
  margin: 0;
  font-size: 12px;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
</style>
