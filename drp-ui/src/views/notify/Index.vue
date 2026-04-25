<template>
  <div class="notify-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">通知管理</h2>
      <div class="page-breadcrumb">
        <span>系统管理</span>
        <span class="separator">/</span>
        <span>通知管理</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid" style="grid-template-columns: repeat(4, 1fr);">
      <div class="stat-card">
        <div class="stat-icon blue"><i class="fas fa-bell"></i></div>
        <div class="stat-info">
          <h3>{{ stats.monthly }}</h3>
          <p>本月发送</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><i class="fas fa-check-circle"></i></div>
        <div class="stat-info">
          <h3>{{ stats.success }}</h3>
          <p>发送成功</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red"><i class="fas fa-times-circle"></i></div>
        <div class="stat-info">
          <h3>{{ stats.failed }}</h3>
          <p>发送失败</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange"><i class="fas fa-clock"></i></div>
        <div class="stat-info">
          <h3>{{ stats.retrying }}</h3>
          <p>重试中</p>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="通知规则" name="config">
          <div class="tab-toolbar">
            <el-button type="primary" @click="handleAddConfig">
              <i class="fas fa-plus"></i> 添加规则
            </el-button>
          </div>
          <el-table :data="configs" stripe v-loading="loading">
            <el-table-column prop="channel" label="渠道" width="120">
              <template #default="{ row }">
                <span style="display:flex;align-items:center;gap:8px;">
                  <i :class="getChannelIcon(row.channel)" :style="{ color: getChannelColor(row.channel) }"></i>
                  {{ getChannelName(row.channel) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="event" label="事件类型" width="150">
              <template #default="{ row }">
                <el-tag :type="getEventSeverityType(row.event)" size="small">
                  {{ getEventName(row.event) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="projectName" label="关联项目" width="150">
              <template #default="{ row }">
                {{ row.projectName || '全局配置' }}
              </template>
            </el-table-column>
            <el-table-column prop="recipients" label="接收人" min-width="150" />
            <el-table-column prop="rateLimit" label="频率限制" width="100">
              <template #default="{ row }">
                {{ row.rateLimit ? row.rateLimit + '秒' : '无' }}
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="80">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="handleToggle(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="success" link @click="handleTest(row)">测试</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadConfigs"
            @current-change="loadConfigs"
          />
        </el-tab-pane>

        <el-tab-pane label="发送记录" name="records">
          <div class="tab-toolbar">
            <el-select v-model="filters.channel" placeholder="渠道" clearable size="small" style="width:120px;">
              <el-option label="全部" value="" />
              <el-option v-for="(info, key) in NotifyChannelMap" :key="key" :label="info.label" :value="key" />
            </el-select>
            <el-select v-model="filters.status" placeholder="状态" clearable size="small" style="width:100px;">
              <el-option label="全部" value="" />
              <el-option v-for="(info, key) in NotifyStatusMap" :key="key" :label="info.label" :value="key" />
            </el-select>
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              style="width:240px;"
            />
            <el-button size="small" @click="handleExport"><i class="fas fa-download"></i> 导出</el-button>
            <el-button size="small" @click="loadRecords"><i class="fas fa-sync"></i> 刷新</el-button>
          </div>
          <el-table :data="records" stripe v-loading="loading">
            <el-table-column prop="sendTime" label="发送时间" width="160" />
            <el-table-column prop="channel" label="渠道" width="100">
              <template #default="{ row }">
                <i :class="getChannelIcon(row.channel)" :style="{ color: getChannelColor(row.channel) }"></i>
                {{ getChannelName(row.channel) }}
              </template>
            </el-table-column>
            <el-table-column prop="event" label="事件" width="120">
              <template #default="{ row }">
                <el-tag :type="getEventSeverityType(row.event)" size="small">{{ getEventName(row.event) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recipient" label="接收人" width="150" />
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{ row }">
                <span style="font-weight:500;">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="NotifyStatusMap[row.status]?.type || 'info'" size="small">
                  {{ NotifyStatusMap[row.status]?.label || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="retryCount" label="重试" width="80">
              <template #default="{ row }">
                {{ row.retryCount || '-' }} / {{ row.maxRetry || 3 }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleViewRecord(row)">详情</el-button>
                <el-button v-if="row.status === 'FAIL'" type="warning" link @click="handleRetry(row)">重试</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadRecords"
            @current-change="loadRecords"
          />
        </el-tab-pane>

        <el-tab-pane label="消息模板" name="templates">
          <div class="tab-toolbar">
            <el-button type="primary" @click="handleAddTemplate">
              <i class="fas fa-plus"></i> 创建模板
            </el-button>
          </div>
          <el-table :data="templates" stripe v-loading="loading">
            <el-table-column prop="name" label="模板名称" width="180" />
            <el-table-column prop="channel" label="渠道" width="100">
              <template #default="{ row }">
                <i :class="getChannelIcon(row.channel)" :style="{ color: getChannelColor(row.channel) }"></i>
                {{ getChannelName(row.channel) }}
              </template>
            </el-table-column>
            <el-table-column prop="event" label="适用事件" width="150">
              <template #default="{ row }">
                <el-tag :type="getEventSeverityType(row.event)" size="small">{{ getEventName(row.event) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="titleTemplate" label="标题模板" min-width="200" show-overflow-tooltip />
            <el-table-column prop="variables" label="变量" width="150" show-overflow-tooltip />
            <el-table-column prop="enabled" label="状态" width="80">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="handleToggleTemplate(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditTemplate(row)">编辑</el-button>
                <el-button type="info" link @click="handlePreviewTemplate(row)">预览</el-button>
                <el-button type="danger" link @click="handleDeleteTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 配置弹窗 -->
    <el-dialog v-model="configDialogVisible" :title="isEditConfig ? '编辑通知规则' : '新增通知规则'" width="600px">
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-width="100px">
        <el-form-item label="项目范围">
          <el-select v-model="configForm.projectId" placeholder="全部项目" clearable>
            <el-option label="全局配置" :value="null" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知渠道" prop="channel">
          <el-select v-model="configForm.channel" placeholder="选择渠道">
            <el-option v-for="(info, key) in NotifyChannelMap" :key="key" :label="info.label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知事件" prop="event">
          <el-select v-model="configForm.event" placeholder="选择事件">
            <el-option-group v-for="(events, category) in groupedEvents" :key="category" :label="category">
              <el-option v-for="e in events" :key="e.value" :label="e.label" :value="e.value" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="接收人" prop="recipients">
          <el-input v-model="configForm.recipients" placeholder="企业微信/钉钉: @用户ID或群ID; 邮件: 邮箱地址" />
        </el-form-item>
        <el-form-item label="渠道配置">
          <el-input v-model="configFormConfigJson" type="textarea" :rows="4" placeholder="JSON格式配置" />
        </el-form-item>
        <el-form-item label="频率限制">
          <el-input-number v-model="configForm.rateLimit" :min="0" :max="3600" :step="30" />
          <span class="form-tip">0表示不限制，单位：秒</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitConfig">确定</el-button>
      </template>
    </el-dialog>

    <!-- 记录详情弹窗 -->
    <el-dialog v-model="recordDialogVisible" title="通知详情" width="700px">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="发送时间">{{ currentRecord.sendTime }}</el-descriptions-item>
        <el-descriptions-item label="渠道">{{ getChannelName(currentRecord.channel) }}</el-descriptions-item>
        <el-descriptions-item label="事件">{{ getEventName(currentRecord.event) }}</el-descriptions-item>
        <el-descriptions-item label="接收人">{{ currentRecord.recipient }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="NotifyStatusMap[currentRecord.status]?.type || 'info'">
            {{ NotifyStatusMap[currentRecord.status]?.label || currentRecord.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="外部ID">{{ currentRecord.externalId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentRecord.retryCount }} / {{ currentRecord.maxRetry }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentRecord.errorMessage">
          <el-alert type="error" :closable="false">{{ currentRecord.errorMessage }}</el-alert>
        </el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">消息内容</el-divider>
      <div class="message-content">
        <h4>{{ currentRecord?.title }}</h4>
        <pre>{{ currentRecord?.content }}</pre>
      </div>
      <template #footer>
        <el-button @click="recordDialogVisible = false">关闭</el-button>
        <el-button v-if="currentRecord?.status === 'FAIL'" type="warning" @click="handleRetry(currentRecord)">
          重试发送
        </el-button>
      </template>
    </el-dialog>

    <!-- 模板弹窗 -->
    <el-dialog v-model="templateDialogVisible" :title="isEditTemplate ? '编辑模板' : '创建模板'" width="600px">
      <el-form ref="templateFormRef" :model="templateForm" :rules="templateRules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="如: 构建成功通知" />
        </el-form-item>
        <el-form-item label="渠道" prop="channel">
          <el-select v-model="templateForm.channel" placeholder="选择渠道">
            <el-option v-for="(info, key) in NotifyChannelMap" :key="key" :label="info.label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用事件" prop="event">
          <el-select v-model="templateForm.event" placeholder="选择事件">
            <el-option-group v-for="(events, category) in groupedEvents" :key="category" :label="category">
              <el-option v-for="e in events" :key="e.value" :label="e.label" :value="e.value" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="标题模板" prop="titleTemplate">
          <el-input v-model="templateForm.titleTemplate" placeholder="如: 【{project}】构建成功" />
        </el-form-item>
        <el-form-item label="内容模板" prop="contentTemplate">
          <el-input v-model="templateForm.contentTemplate" type="textarea" :rows="4" placeholder="支持变量: {project}, {env}, {version}, {operator}, {time}" />
        </el-form-item>
        <el-form-item label="变量说明">
          <el-input v-model="templateForm.variables" type="textarea" :rows="2" placeholder="如: project=项目名, env=环境, version=版本号" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="templateForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTemplate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 模板预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="模板预览" width="600px">
      <div class="preview-content" v-html="previewContent"></div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNotifyConfigs,
  createNotifyConfig,
  updateNotifyConfig,
  deleteNotifyConfig,
  testNotifyChannel,
  getNotifyRecords,
  retryNotifyRecord,
  getNotifyTemplates,
  createNotifyTemplate,
  updateNotifyTemplate,
  deleteNotifyTemplate,
  NotifyChannelMap,
  NotifyEventMap,
  NotifyStatusMap,
  type NotificationConfig,
  type NotificationRecord,
  type NotificationTemplate
} from '@/api/notify'

const activeTab = ref('config')
const loading = ref(false)
const configDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const templateDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const isEditConfig = ref(false)
const isEditTemplate = ref(false)
const configFormRef = ref()
const templateFormRef = ref()

const configs = ref<NotificationConfig[]>([])
const records = ref<NotificationRecord[]>([])
const templates = ref<NotificationTemplate[]>([])
const currentRecord = ref<NotificationRecord | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const filters = reactive({
  channel: '',
  status: '',
  dateRange: [] as string[]
})

const stats = reactive({
  monthly: 128,
  success: 124,
  failed: 4,
  retrying: 2
})

const configForm = reactive<Partial<NotificationConfig>>({
  id: undefined,
  projectId: undefined,
  channel: '',
  event: '',
  enabled: true,
  config: {},
  recipients: '',
  rateLimit: 60
})

const templateForm = reactive<Partial<NotificationTemplate>>({
  id: undefined,
  name: '',
  channel: '',
  event: '',
  titleTemplate: '',
  contentTemplate: '',
  variables: '',
  enabled: true
})

const previewContent = ref('')

const configFormConfigJson = ref('')

const configRules = {
  channel: [{ required: true, message: '请选择渠道', trigger: 'change' }],
  event: [{ required: true, message: '请选择事件', trigger: 'change' }],
  recipients: [{ required: true, message: '请输入接收人', trigger: 'blur' }]
}

const templateRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  channel: [{ required: true, message: '请选择渠道', trigger: 'change' }],
  event: [{ required: true, message: '请选择事件', trigger: 'change' }],
  titleTemplate: [{ required: true, message: '请输入标题模板', trigger: 'blur' }],
  contentTemplate: [{ required: true, message: '请输入内容模板', trigger: 'blur' }]
}

const groupedEvents = computed(() => {
  const groups: Record<string, { label: string; value: string }[]> = {}
  for (const [key, info] of Object.entries(NotifyEventMap)) {
    if (!groups[info.category]) {
      groups[info.category] = []
    }
    groups[info.category].push({ label: info.label, value: key })
  }
  return groups
})

function getChannelIcon(channel: string) {
  return NotifyChannelMap[channel]?.icon || 'fas fa-bell'
}

function getChannelName(channel: string) {
  return NotifyChannelMap[channel]?.label || channel
}

function getChannelColor(channel: string) {
  return NotifyChannelMap[channel]?.color || '#909399'
}

function getEventName(event: string) {
  return NotifyEventMap[event]?.label || event
}

function getEventSeverityType(event: string) {
  const severity = NotifyEventMap[event]?.severity
  return { INFO: 'info', WARNING: 'warning', CRITICAL: 'danger' }[severity || 'INFO'] || 'info'
}

async function loadConfigs() {
  loading.value = true
  try {
    const res = await getNotifyConfigs({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    configs.value = res.records
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await getNotifyRecords({
      channel: filters.channel || undefined,
      status: filters.status || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    records.value = res.records
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

async function loadTemplates() {
  loading.value = true
  try {
    const res = await getNotifyTemplates({ page: 1, pageSize: 100 })
    templates.value = res.records
  } finally {
    loading.value = false
  }
}

function handleTabChange(tab: string) {
  if (tab === 'config') {
    loadConfigs()
  } else if (tab === 'records') {
    loadRecords()
  } else if (tab === 'templates') {
    loadTemplates()
  }
}

function handleAddConfig() {
  isEditConfig.value = false
  Object.assign(configForm, {
    id: null,
    projectId: null,
    channel: '',
    event: '',
    enabled: true,
    config: {},
    recipients: '',
    rateLimit: 60
  })
  configFormConfigJson.value = ''
  configDialogVisible.value = true
}

function handleEdit(row: NotificationConfig) {
  isEditConfig.value = true
  Object.assign(configForm, row)
  configFormConfigJson.value = JSON.stringify(row.config || {}, null, 2)
  configDialogVisible.value = true
}

async function handleToggle(row: NotificationConfig) {
  try {
    await updateNotifyConfig(row.id!, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
  } catch {
    row.enabled = !row.enabled
  }
}

async function handleSubmitConfig() {
  const valid = await configFormRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    configForm.config = JSON.parse(configFormConfigJson.value || '{}')
    if (isEditConfig.value) {
      await updateNotifyConfig(configForm.id!, configForm)
      ElMessage.success('更新成功')
    } else {
      await createNotifyConfig(configForm)
      ElMessage.success('创建成功')
    }
    configDialogVisible.value = false
    loadConfigs()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleTest(row: NotificationConfig) {
  try {
    await ElMessageBox.confirm('确认发送测试通知？', '测试通知', { type: 'info' })
    await testNotifyChannel({ channel: row.channel, config: row.config || {} })
    ElMessage.success('测试通知已发送')
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('发送失败')
    }
  }
}

async function handleDelete(row: NotificationConfig) {
  try {
    await ElMessageBox.confirm('确认删除该配置？', '删除确认', { type: 'warning' })
    await deleteNotifyConfig(row.id!)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function handleViewRecord(row: NotificationRecord) {
  currentRecord.value = row
  recordDialogVisible.value = true
}

async function handleRetry(row: NotificationRecord) {
  try {
    await retryNotifyRecord(row.id)
    ElMessage.success('已加入重试队列')
    loadRecords()
    recordDialogVisible.value = false
  } catch {
    ElMessage.error('重试失败')
  }
}

function handleExport() {
  ElMessage.info('导出功能开发中')
}

function handleAddTemplate() {
  isEditTemplate.value = false
  Object.assign(templateForm, {
    id: undefined,
    name: '',
    channel: '',
    event: '',
    titleTemplate: '',
    contentTemplate: '',
    variables: '',
    enabled: true
  })
  templateDialogVisible.value = true
}

function handleEditTemplate(row: NotificationTemplate) {
  isEditTemplate.value = true
  Object.assign(templateForm, {
    id: row.id,
    name: row.name,
    channel: row.channel,
    event: row.event,
    titleTemplate: row.titleTemplate,
    contentTemplate: row.contentTemplate,
    variables: row.variables || '',
    enabled: row.enabled
  })
  templateDialogVisible.value = true
}

function handlePreviewTemplate(row: NotificationTemplate) {
  const vars = (row.variables || '').split(',').reduce((acc: Record<string, string>, v: string) => {
    const [key] = v.split('=')
    if (key) acc[key.trim()] = `{${key.trim()}}`
    return acc
  }, {})

  let content = row.contentTemplate
  for (const [key, val] of Object.entries(vars)) {
    content = content.replace(new RegExp(`\\{${key}\\}`, 'g'), val as string)
  }

  previewContent.value = `
    <div style="padding: 10px; background: #f5f5f5; border-radius: 4px;">
      <h3 style="margin-top: 0;">${row.titleTemplate}</h3>
      <pre style="white-space: pre-wrap; word-break: break-all;">${content}</pre>
      <div style="margin-top: 10px; color: #666; font-size: 12px;">
        <strong>变量说明:</strong> ${row.variables || '无'}
      </div>
    </div>
  `
  previewDialogVisible.value = true
}

async function handleDeleteTemplate(row: NotificationTemplate) {
  try {
    await ElMessageBox.confirm('确认删除该模板？', '删除确认', { type: 'warning' })
    await deleteNotifyTemplate(row.id!)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleSubmitTemplate() {
  try {
    await templateFormRef.value.validate()
    if (isEditTemplate.value) {
      await updateNotifyTemplate(templateForm.id!, templateForm as NotificationTemplate)
      ElMessage.success('更新成功')
    } else {
      await createNotifyTemplate(templateForm as NotificationTemplate)
      ElMessage.success('创建成功')
    }
    templateDialogVisible.value = false
    loadTemplates()
  } catch (e: any) {
    if (e !== false) {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function handleToggleTemplate(row: NotificationTemplate) {
  try {
    await updateNotifyTemplate(row.id!, { enabled: row.enabled })
  } catch {
    row.enabled = !row.enabled
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.notify-center {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  margin-bottom: 8px;
}

.page-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.separator {
  margin: 0 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.blue { background: linear-gradient(135deg, #409eff, #66b1ff); }
.stat-icon.green { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-icon.red { background: linear-gradient(135deg, #f56c6c, #f78989); }
.stat-icon.orange { background: linear-gradient(135deg, #e6a23c, #ebb563); }

.stat-info h3 {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
}

.stat-info p {
  color: #909399;
  font-size: 14px;
  margin: 4px 0 0 0;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tab-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.form-tip {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}

.message-content {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
}

.message-content h4 {
  margin: 0 0 12px 0;
}

.message-content pre {
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

.preview-content {
  padding: 0;
}
</style>
