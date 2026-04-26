<template>
  <div class="page-container">
    <el-page-header @back="() => $router.back()" content="创建部署" />
    <div class="create-grid" style="margin-top: 20px;">
      <div class="card-container">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
          <el-form-item label="项目" prop="projectId">
            <el-select v-model="form.projectId" placeholder="请选择项目" filterable @change="handleProjectChange">
              <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="构建版本" prop="buildId">
            <el-select v-model="form.buildId" placeholder="请选择构建版本" filterable>
              <el-option v-for="item in buildList" :key="item.id" :label="`${item.version || '#'+item.buildNumber} / ${item.branch}`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="部署环境" prop="env">
            <el-radio-group v-model="form.env">
              <el-radio-button label="dev">开发</el-radio-button>
              <el-radio-button label="test">测试</el-radio-button>
              <el-radio-button label="pre">预发</el-radio-button>
              <el-radio-button label="prod">生产</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="部署策略" prop="strategy">
            <el-select v-model="form.strategy" placeholder="请选择部署策略">
              <el-option label="单机部署" value="SINGLE" />
              <el-option label="滚动发布" value="ROLLING" />
              <el-option label="蓝绿发布" value="BLUE_GREEN" />
              <el-option label="灰度发布" value="GRAY" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标服务器">
            <el-select v-model="form.serverIds" multiple placeholder="按环境选择服务器" filterable>
              <el-option v-for="item in serverOptions" :key="item.id" :label="`${item.name} (${item.hostname})`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="触发方式">
            <el-select v-model="form.triggerType">
              <el-option label="手动触发" value="MANUAL" />
              <el-option label="自动触发" value="AUTO" />
              <el-option label="接口触发" value="API" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布窗口">
            <el-input v-model="form.deployWindow" placeholder="例如 22:00-23:59" />
          </el-form-item>
          <el-form-item label="审批开关">
            <el-switch v-model="form.requireApproval" active-text="需要审批" inactive-text="无需审批" />
          </el-form-item>
          <el-form-item label="自动回滚">
            <el-switch v-model="form.autoRollback" active-text="开启" inactive-text="关闭" />
          </el-form-item>
          <template v-if="form.strategy === 'GRAY'">
            <el-form-item label="灰度比例(%)">
              <el-input-number v-model="form.grayPercent" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="批次间隔(秒)">
              <el-input-number v-model="form.grayInterval" :min="10" :max="3600" />
            </el-form-item>
          </template>
          <el-form-item label="变更单号">
            <el-input v-model="form.changeTicket" placeholder="CHG-20260420-001" />
          </el-form-item>
          <el-form-item>
            <el-button @click="handlePreview" :loading="previewLoading">部署预览</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">提交部署</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="card-container preview-panel">
        <div class="panel-title">部署预览</div>
        <el-empty v-if="!preview" description="请先填写参数并执行部署预览" />
        <template v-else>
          <div class="preview-summary">
            <div><strong>项目：</strong>{{ preview.projectName }}</div>
            <div><strong>版本：</strong>{{ preview.version }}</div>
            <div><strong>风险：</strong><el-tag :type="getRiskType(preview.riskLevel)">{{ preview.riskLevelDesc }}</el-tag></div>
            <div><strong>审批：</strong>{{ preview.requireApproval ? '需要审批' : '无需审批' }}</div>
            <div><strong>窗口：</strong>{{ preview.windowValid ? '命中发布窗口' : '不在发布窗口' }}</div>
          </div>

          <div class="section-title">校验项</div>
          <div class="check-list">
            <div v-for="item in preview.checks" :key="item.name" class="check-item">
              <el-tag :type="getCheckType(item.status)">{{ item.status }}</el-tag>
              <span class="check-name">{{ item.name }}</span>
              <span class="check-message">{{ item.message }}</span>
            </div>
          </div>

          <div class="section-title">目标服务器</div>
          <div class="server-list">
            <div v-for="item in preview.servers" :key="item.id" class="server-item">
              <div>{{ item.name }}</div>
              <div class="server-meta">{{ item.hostname }} / {{ item.deployPath }}</div>
            </div>
          </div>

          <div class="section-title">风险提示</div>
          <ul class="warning-list">
            <li v-for="(item, index) in preview.warnings" :key="index">{{ item }}</li>
          </ul>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getProjectPage, type Project } from '@/api/project'
import { getBuildList, type Build } from '@/api/build'
import { getServerAll, type Server } from '@/api/server'
import { createDeploy, previewDeploy, type DeployCreateRequest, type DeployPreview } from '@/api/deploy'

const router = useRouter()
const formRef = ref<FormInstance>()
const projectList = ref<Project[]>([])
const buildList = ref<Build[]>([])
const serverList = ref<Server[]>([])
const preview = ref<DeployPreview>()
const previewLoading = ref(false)
const submitting = ref(false)

const form = reactive<DeployCreateRequest>({
  projectId: 0,
  buildId: 0,
  env: 'test',
  strategy: 'ROLLING',
  serverIds: [],
  requireApproval: false,
  deployWindow: '22:00-23:59',
  grayPercent: 10,
  grayInterval: 120,
  triggerType: 'MANUAL',
  changeTicket: '',
  autoRollback: true
})

const rules: FormRules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  buildId: [{ required: true, message: '请选择构建版本', trigger: 'change' }],
  env: [{ required: true, message: '请选择环境', trigger: 'change' }],
  strategy: [{ required: true, message: '请选择策略', trigger: 'change' }]
}

const serverOptions = computed(() => serverList.value.filter(item => !form.env || item.env === form.env))

onMounted(() => {
  loadProjects()
  loadServers()
})

async function loadProjects() {
  const data = await getProjectPage({ page: 1, pageSize: 100 })
  projectList.value = data.records || []
}

async function loadServers() {
  serverList.value = await getServerAll()
}

async function handleProjectChange(projectId: number) {
  const data = await getBuildList({ projectId, status: 2, page: 1, pageSize: 100 })
  buildList.value = data.records || []
  form.buildId = 0
  form.serverIds = []
}

async function handlePreview() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    previewLoading.value = true
    try {
      preview.value = await previewDeploy({ ...form })
      ElMessage.success('预览完成')
    } finally {
      previewLoading.value = false
    }
  })
}

async function handleSubmit() {
  if (!preview.value) {
    ElMessage.warning('请先执行部署预览')
    return
  }
  submitting.value = true
  try {
    const detail = await createDeploy({ ...form })
    ElMessage.success('部署任务已创建')
    router.push(`/deploys/${detail.id}`)
  } finally {
    submitting.value = false
  }
}

function getRiskType(level: string) {
  return ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' } as Record<string, string>)[level] || 'info'
}

function getCheckType(status: string) {
  return ({ PASS: 'success', WARN: 'warning', FAIL: 'danger' } as Record<string, string>)[status] || 'info'
}
</script>

<style scoped lang="scss">
.create-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 16px;
}

.preview-panel {
  min-height: 520px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.preview-summary {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.section-title {
  font-weight: 600;
  margin: 14px 0 10px;
}

.check-list,
.server-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.check-item,
.server-item {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px;
}

.check-name {
  margin-left: 10px;
  font-weight: 500;
}

.check-message,
.server-meta {
  display: block;
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}

.warning-list {
  margin: 0;
  padding-left: 20px;
  color: #e6a23c;
}
</style>
