<template>
  <div class="page-container" v-loading="loading">
    <el-page-header @back="() => $router.back()" content="部署详情" />
    <template v-if="detail">
      <div class="summary-card card-container" style="margin-top: 20px;">
        <div class="summary-header">
          <div>
            <div class="summary-title">{{ detail.projectName }} / {{ detail.version }}</div>
            <div class="summary-meta">{{ getEnvLabel(detail.env) }} · {{ detail.strategy }} · {{ detail.triggerUser }}</div>
          </div>
          <div class="summary-tags">
            <el-tag :type="statusTag">{{ detail.statusDesc }}</el-tag>
            <el-tag :type="approvalTag">{{ detail.approvalStatusDesc }}</el-tag>
            <el-tag :type="riskTag">{{ detail.riskLevelDesc }}</el-tag>
          </div>
        </div>
        <div class="summary-grid">
          <div><strong>当前阶段：</strong>{{ detail.currentStep }}</div>
          <div><strong>分支：</strong>{{ detail.branch }}</div>
          <div><strong>发布窗口：</strong>{{ detail.deployWindow || '-' }}</div>
          <div><strong>变更单：</strong>{{ detail.changeTicket || '-' }}</div>
          <div><strong>自动回滚：</strong>{{ detail.autoRollback ? '开启' : '关闭' }}</div>
          <div><strong>耗时：</strong>{{ formatDuration(detail.duration) }}</div>
        </div>
      </div>

      <div class="detail-grid">
        <div class="card-container">
          <div class="panel-title">阶段时间线</div>
          <el-timeline>
            <el-timeline-item v-for="item in detail.steps" :key="item.stepKey" :type="getStepType(item.status)" :timestamp="formatDuration(item.duration)">
              <div class="timeline-title">{{ item.stepName }}</div>
              <div class="timeline-detail">{{ item.detail }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="card-container">
          <div class="panel-title">健康检查</div>
          <div class="check-list">
            <div v-for="item in detail.healthChecks" :key="item.name" class="check-item">
              <el-tag :type="getCheckType(item.status)">{{ item.status }}</el-tag>
              <span class="check-name">{{ item.name }}</span>
              <span class="check-message">{{ item.message }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-grid second-row">
        <div class="card-container">
          <div class="panel-title">目标服务器</div>
          <el-table :data="detail.servers" stripe>
            <el-table-column prop="name" label="服务器" />
            <el-table-column prop="hostname" label="地址" />
            <el-table-column prop="deployPath" label="部署路径" />
            <el-table-column prop="statusDesc" label="状态" width="90" />
          </el-table>
        </div>

        <div class="card-container">
          <div class="panel-title">审批记录</div>
          <el-empty v-if="!detail.approvalRecords.length" description="暂无审批记录" />
          <el-timeline v-else>
            <el-timeline-item v-for="item in detail.approvalRecords" :key="item.id" :timestamp="item.approveTime || item.createTime">
              <div class="timeline-title">{{ item.statusDesc }}</div>
              <div class="timeline-detail">{{ item.approver || '系统' }} {{ item.comment ? `- ${item.comment}` : '' }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <div class="card-container" style="margin-top: 16px;">
        <div class="panel-title">部署日志</div>
        <pre class="log-panel">{{ detail.logContent || '暂无日志' }}</pre>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { DeployStatusMap, getDeployDetail, type DeployDetail } from '@/api/deploy'

const route = useRoute()
const loading = ref(false)
const detail = ref<DeployDetail>()

const statusTag = computed(() => detail.value ? DeployStatusMap[detail.value.status]?.type : 'info')
const approvalTag = computed(() => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', NOT_REQUIRED: 'info' } as Record<string, string>)[detail.value?.approvalStatus || 'NOT_REQUIRED'])
const riskTag = computed(() => ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' } as Record<string, string>)[detail.value?.riskLevel || 'LOW'])

onMounted(loadDetail)

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await getDeployDetail(Number(route.params.id))
  } finally {
    loading.value = false
  }
}

function getEnvLabel(env?: string) {
  return ({ dev: '开发', test: '测试', pre: '预发', prod: '生产' } as Record<string, string>)[env || ''] || env || '-'
}

function formatDuration(seconds?: number) {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}分${secs}秒` : `${secs}秒`
}

function getCheckType(status: string) {
  return ({ PASS: 'success', WARN: 'warning', FAIL: 'danger' } as Record<string, string>)[status] || 'info'
}

function getStepType(status: string) {
  return ({ SUCCESS: 'success', FAIL: 'danger', RUNNING: 'primary' } as Record<string, string>)[status] || 'info'
}
</script>

<style scoped lang="scss">
.summary-card,
.card-container {
  background: #fff;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.summary-title {
  font-size: 20px;
  font-weight: 700;
}

.summary-meta {
  color: #909399;
  margin-top: 8px;
}

.summary-tags {
  display: flex;
  gap: 8px;
}

.summary-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.check-item {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px;
}

.check-name {
  margin-left: 10px;
  font-weight: 500;
}

.check-message,
.timeline-detail {
  display: block;
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.timeline-title {
  font-weight: 600;
}

.log-panel {
  max-height: 420px;
  overflow: auto;
  background: #111827;
  color: #d1fae5;
  border-radius: 10px;
  padding: 16px;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
