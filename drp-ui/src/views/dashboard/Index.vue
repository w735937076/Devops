<!--
  DRP Platform - 首页仪表盘

  @author Nick
-->
<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">你好，{{ userStore.realName || userStore.username }}！</h1>
        <p class="welcome-subtitle">欢迎使用 DRP DevOps 平台，祝您工作愉快</p>
      </div>
      <div class="welcome-time">
        <span class="date">{{ currentDate }}</span>
        <span class="time">{{ currentTime }}</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="stat in statsData" :key="stat.key" class="stat-card">
        <div class="stat-icon" :style="{ background: stat.color }">
          <el-icon :size="28">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口 & 最新动态 -->
    <div class="content-grid">
      <!-- 快捷入口 -->
      <div class="quick-actions card">
        <div class="card-header">
          <h3>快捷入口</h3>
        </div>
        <div class="quick-actions-grid">
          <div
            v-for="action in quickActions"
            :key="action.path"
            class="quick-action-item"
            @click="router.push(action.path)"
          >
            <div class="action-icon" :style="{ background: action.color }">
              <el-icon :size="24">
                <component :is="action.icon" />
              </el-icon>
            </div>
            <span class="action-label">{{ action.label }}</span>
          </div>
        </div>
      </div>

      <!-- 最新构建 -->
      <div class="recent-builds card">
        <div class="card-header">
          <h3>最新构建</h3>
          <el-button type="primary" link @click="router.push('/builds')">
            查看更多
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <el-table :data="recentBuilds" size="small">
          <el-table-column prop="projectName" label="项目" min-width="120" />
          <el-table-column prop="branch" label="分支" width="80">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.branch }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="70">
            <template #default="{ row }">
              <el-tag size="small" :type="getBuildStatusType(row.status)">
                {{ getBuildStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="100">
            <template #default="{ row }">
              {{ formatRelativeTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 最新部署 -->
      <div class="recent-deploys card">
        <div class="card-header">
          <h3>最新部署</h3>
          <el-button type="primary" link @click="router.push('/deploys')">
            查看更多
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <el-table :data="recentDeploys" size="small">
          <el-table-column prop="projectName" label="项目" min-width="120" />
          <el-table-column prop="env" label="环境" width="70">
            <template #default="{ row }">
              <el-tag size="small" :type="getEnvType(row.env)">{{ row.env }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="70">
            <template #default="{ row }">
              <el-tag size="small" :type="getDeployStatusType(row.status)">
                {{ getDeployStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="100">
            <template #default="{ row }">
              {{ formatRelativeTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 首页仪表盘
 *
 * 功能：
 * - 欢迎信息
 * - 统计卡片
 * - 快捷入口
 * - 最新构建/部署列表
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { formatRelativeTime } from '@/utils/date'
import {
  FolderOpened,
  Box,
  Upload,
  Monitor,
  Document,
  Setting,
  Plus,
  ArrowRight
} from '@element-plus/icons-vue'

// Router
const router = useRouter()

// Store
const userStore = useUserStore()

// 当前时间
const currentDate = ref('')
const currentTime = ref('')
let timeTimer: number

// 统计卡片数据
const statsData = ref([
  { key: 'projects', label: '项目总数', value: 12, icon: FolderOpened, color: '#667eea' },
  { key: 'builds', label: '构建次数', value: 156, icon: Box, color: '#409eff' },
  { key: 'deploys', label: '部署次数', value: 89, icon: Upload, color: '#67c23a' },
  { key: 'servers', label: '服务器', value: 8, icon: Monitor, color: '#e6a23c' }
])

// 快捷入口
const quickActions = [
  { label: '创建项目', icon: Plus, path: '/projects', color: '#667eea' },
  { label: '触发构建', icon: Box, path: '/builds', color: '#409eff' },
  { label: '执行部署', icon: Upload, path: '/deploys/create', color: '#67c23a' },
  { label: '服务器管理', icon: Monitor, path: '/servers', color: '#e6a23c' },
  { label: '日志中心', icon: Document, path: '/logs', color: '#f56c6c' },
  { label: '系统设置', icon: Setting, path: '/system/users', color: '#909399' }
]

// 最新构建数据（模拟）
const recentBuilds = ref([
  { id: 1, projectName: 'drp-service', branch: 'master', status: 2, createTime: Date.now() - 300000 },
  { id: 2, projectName: 'drp-frontend', branch: 'develop', status: 2, createTime: Date.now() - 600000 },
  { id: 3, projectName: 'user-service', branch: 'feature/auth', status: 3, createTime: Date.now() - 900000 }
])

// 最新部署数据（模拟）
const recentDeploys = ref([
  { id: 1, projectName: 'drp-service', env: 'prod', status: 2, createTime: Date.now() - 1800000 },
  { id: 2, projectName: 'drp-service', env: 'test', status: 2, createTime: Date.now() - 3600000 },
  { id: 3, projectName: 'user-service', env: 'prod', status: 3, createTime: Date.now() - 7200000 }
])

// 更新当前时间
function updateTime() {
  const now = new Date()
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取构建状态类型
function getBuildStatusType(status: number) {
  const map: Record<number, string> = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'warning'
  }
  return map[status] || 'info'
}

// 获取构建状态文本
function getBuildStatusText(status: number) {
  const map: Record<number, string> = {
    0: '等待',
    1: '构建中',
    2: '成功',
    3: '失败',
    4: '取消'
  }
  return map[status] || '未知'
}

// 获取环境标签类型
function getEnvType(env: string) {
  const map: Record<string, string> = {
    dev: 'info',
    test: 'warning',
    pre: 'primary',
    prod: 'danger'
  }
  return map[env] || 'info'
}

// 获取部署状态类型
function getDeployStatusType(status: number) {
  const map: Record<number, string> = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'warning'
  }
  return map[status] || 'info'
}

// 获取部署状态文本
function getDeployStatusText(status: number) {
  const map: Record<number, string> = {
    0: '等待',
    1: '部署中',
    2: '成功',
    3: '失败',
    4: '回滚'
  }
  return map[status] || '未知'
}

// 生命周期
onMounted(() => {
  updateTime()
  timeTimer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timeTimer)
})
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

// 欢迎区域
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px;
}

.welcome-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.welcome-time {
  text-align: right;

  .date {
    display: block;
    font-size: 14px;
    opacity: 0.9;
  }

  .time {
    font-size: 24px;
    font-weight: 600;
    font-family: 'Roboto Mono', monospace;
  }
}

// 统计卡片
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  color: #fff;
  margin-right: 16px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

// 内容区域
.content-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  @media (max-width: 1200px) {
    grid-template-columns: 1fr;
  }
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

// 快捷入口
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f7fa;
  }
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  color: #fff;
  margin-bottom: 8px;
}

.action-label {
  font-size: 13px;
  color: #606266;
}
</style>
