<template>
  <div class="page-container">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="page-title-area">
        <div class="page-icon">
          <i class="fas fa-server"></i>
        </div>
        <div class="page-title-content">
          <h2 class="page-title">服务器管理</h2>
          <p class="page-desc">管理所有部署目标服务器，包括服务器配置、连接测试、主机分组等功能</p>
        </div>
      </div>
      <div class="header-actions">
        <el-select v-model="queryParams.group" placeholder="全部分组" clearable style="width: 150px; margin-right: 12px;" @change="handleQuery">
          <el-option v-for="group in groups" :key="group.code" :label="group.name" :value="group.code" />
        </el-select>
        <el-button type="primary" @click="handleCreate">
          <i class="fas fa-plus"></i> 添加服务器
        </el-button>
      </div>
    </div>

    <div class="card-container">
      <el-table :data="servers" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="服务器名称" min-width="180">
          <template #default="{ row }">
            <div class="server-name-cell">
              <div class="server-icon">
                <i class="fas fa-server"></i>
              </div>
              <div class="server-info">
                <div class="server-name">{{ row.name }}</div>
                <div class="server-host">{{ row.hostname }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="groups" label="分组" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ getGroupName(row.groups) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="env" label="环境" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.env" :type="getEnvType(row.env)" size="small">{{ getEnvLabel(row.env) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <span v-if="row.tags">{{ row.tags }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="port" label="SSH端口" width="100" />
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <div class="status-cell">
              <span class="status-dot" :class="getStatusClass(row.status)"></span>
              <span>{{ row.statusDesc }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="180">
          <template #default="{ row }">
            {{ row.lastHeartbeat ? formatDate(row.lastHeartbeat) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleTest(row)">测试</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 分组统计卡片 -->
    <div class="group-stats">
      <div v-for="group in groupStats" :key="group.code" class="stat-card" @click="filterByGroup(group.code)">
        <div class="stat-icon" :class="group.iconClass">
          <i :class="group.icon"></i>
        </div>
        <div class="stat-info">
          <h3>{{ group.count }}</h3>
          <p>{{ group.name }}</p>
        </div>
      </div>
    </div>

    <!-- 添加/编辑服务器弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="服务器名称" prop="name">
          <el-input v-model="form.name" placeholder="如：生产-Web-01" />
        </el-form-item>
        <el-form-item label="主机地址" prop="hostname">
          <el-input v-model="form.hostname" placeholder="IP或主机名" />
        </el-form-item>
        <el-form-item label="SSH端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="root" />
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-radio-group v-model="form.authType">
            <el-radio label="password">密码认证</el-radio>
            <el-radio label="key">私钥认证</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.authType === 'password'" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="form.authType === 'key'" label="私钥" prop="privateKey">
          <el-input v-model="form.privateKey" type="textarea" :rows="5" placeholder="粘贴 SSH 私钥内容" />
        </el-form-item>
        <el-form-item label="服务器分组" prop="groups">
          <el-select v-model="form.groups" placeholder="请选择分组" style="width: 100%;">
            <el-option v-for="group in groups" :key="group.code" :label="group.name" :value="group.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="环境" prop="env">
          <el-select v-model="form.env" placeholder="请选择环境" style="width: 100%;">
            <el-option label="开发环境" value="dev" />
            <el-option label="测试环境" value="test" />
            <el-option label="生产环境" value="prod" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
        <el-form-item label="工作目录">
          <el-input v-model="form.workDir" placeholder="/opt/drp" />
        </el-form-item>
        <el-form-item label="备份目录">
          <el-input v-model="form.backupDir" placeholder="/opt/drp/backup" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getServerList,
  getServerGroupList,
  createServer,
  updateServer,
  deleteServer,
  testServerConnection,
  type Server,
  type ServerGroup,
  type ServerQuery,
  ServerStatus
} from '@/api/server'

const loading = ref(false)
const servers = ref<Server[]>([])
const groups = ref<ServerGroup[]>([])
const total = ref(0)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const queryParams = reactive<ServerQuery>({
  page: 1,
  pageSize: 10,
  group: undefined
})

const form = reactive<any>({
  id: undefined,
  name: '',
  hostname: '',
  port: 22,
  username: '',
  authType: 'password',
  password: '',
  privateKey: '',
  groups: '',
  env: '',
  tags: '',
  remark: '',
  workDir: '/opt/drp',
  backupDir: '/opt/drp/backup'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入服务器名称', trigger: 'blur' }],
  hostname: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  privateKey: [{ required: true, message: '请输入私钥', trigger: 'blur' }]
}

const groupStats = computed(() => {
  return groups.value.map((group, index) => {
    const iconClasses = ['blue', 'green', 'orange', 'red']
    const icons = ['fa-globe', 'fa-cogs', 'fa-database', 'fa-plug']
    return {
      ...group,
      count: servers.value.filter(s => s.groups?.includes(group.code)).length,
      icon: `fas ${icons[index % icons.length]}`,
      iconClass: iconClasses[index % iconClasses.length]
    }
  })
})

function getGroupName(groupCode?: string): string {
  if (!groupCode) return '-'
  const group = groups.value.find(g => g.code === groupCode)
  return group?.name || groupCode
}

function getEnvLabel(env?: string): string {
  const envMap: Record<string, string> = {
    dev: '开发',
    test: '测试',
    prod: '生产'
  }
  return env ? (envMap[env] || env) : '-'
}

function getEnvType(env: string): string {
  const envTypeMap: Record<string, string> = {
    dev: 'success',
    test: 'warning',
    prod: 'danger'
  }
  return envTypeMap[env] || 'info'
}

function getStatusClass(status: number): string {
  switch (status) {
    case ServerStatus.ONLINE:
      return 'online'
    case ServerStatus.BUSY:
      return 'busy'
    default:
      return 'offline'
  }
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function loadServers() {
  loading.value = true
  try {
    const res: any = await getServerList(queryParams)
    servers.value = res.records || []
    total.value = res.total || 0
  } catch {
    servers.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadGroups() {
  try {
    const res = await getServerGroupList()
    groups.value = res as unknown as ServerGroup[]
  } catch {
    groups.value = []
  }
}

function handleQuery() {
  queryParams.page = 1
  loadServers()
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  loadServers()
}

function handleCurrentChange(page: number) {
  queryParams.page = page
  loadServers()
}

function handleCreate() {
  dialogTitle.value = '添加服务器'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Server) {
  dialogTitle.value = '编辑服务器'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    hostname: row.hostname,
    port: row.port,
    username: row.username,
    authType: row.privateKey ? 'key' : 'password',
    password: '',
    privateKey: '',
    groups: row.groups || '',
    env: row.env || '',
    tags: row.tags || '',
    remark: row.remark || '',
    workDir: row.workDir || '/opt/drp',
    backupDir: row.backupDir || '/opt/drp/backup'
  })
  dialogVisible.value = true
}

async function handleTest(row: Server) {
  try {
    const res = await testServerConnection(row.id)
    const result = res as unknown as { success: boolean; message: string; costMs: number }
    if (result.success) {
      ElMessage.success(`连接成功 (${result.costMs}ms)`)
    } else {
      ElMessage.error(`连接失败: ${result.message}`)
    }
  } catch {
    ElMessage.error('测试连接失败')
  }
}

async function handleDelete(row: Server) {
  try {
    await ElMessageBox.confirm(`确定要删除服务器 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteServer(row.id)
    ElMessage.success('删除成功')
    loadServers()
  } catch {
    // 用户取消
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data: any = {
      name: form.name,
      hostname: form.hostname,
      port: form.port,
      username: form.username,
      groups: form.groups,
      env: form.env,
      tags: form.tags,
      remark: form.remark,
      workDir: form.workDir,
      backupDir: form.backupDir
    }

    if (form.authType === 'password') {
      data.password = form.password
    } else {
      data.privateKey = form.privateKey
    }

    if (form.id) {
      await updateServer(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createServer(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadServers()
  } catch {
    // 错误已在请求拦截器处理
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    name: '',
    hostname: '',
    port: 22,
    username: '',
    authType: 'password',
    password: '',
    privateKey: '',
    groups: '',
    env: '',
    tags: '',
    remark: '',
    workDir: '/opt/drp',
    backupDir: '/opt/drp/backup'
  })
  formRef.value?.clearValidate()
}

function filterByGroup(code: string) {
  queryParams.group = code
  handleQuery()
}

onMounted(() => {
  loadServers()
  loadGroups()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;

  .page-title-area {
    display: flex;
    align-items: center;
    gap: 16px;

    .page-icon {
      width: 56px;
      height: 56px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 24px;
        color: #fff;
      }
    }

    .page-title-content {
      .page-title {
        font-size: 20px;
        font-weight: 600;
        margin: 0 0 4px 0;
        color: #fff;
      }

      .page-desc {
        font-size: 14px;
        margin: 0;
        opacity: 0.9;
      }
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    :deep(.el-select) {
      .el-input__wrapper {
        background: rgba(255, 255, 255, 0.95);
        box-shadow: none;
      }
    }

    :deep(.el-button) {
      &.el-button--primary {
        background: #fff;
        border-color: #fff;
        color: #667eea;

        &:hover {
          background: rgba(255, 255, 255, 0.9);
          border-color: rgba(255, 255, 255, 0.9);
          color: #764ba2;
        }
      }
    }
  }
}

.card-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.server-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;

  .server-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 18px;
      color: #606266;
    }
  }

  .server-info {
    .server-name {
      font-weight: 500;
    }

    .server-host {
      font-size: 12px;
      color: #909399;
    }
  }
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;

    &.online {
      background: #67c23a;
    }

    &.offline {
      background: #f56c6c;
    }

    &.busy {
      background: #e6a23c;
    }
  }
}

.group-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;

  .stat-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    cursor: pointer;
    transition: box-shadow 0.3s;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;

      &.blue {
        background: rgba(64, 158, 255, 0.1);
        color: #409eff;
      }

      &.green {
        background: rgba(103, 194, 58, 0.1);
        color: #67c23a;
      }

      &.orange {
        background: rgba(230, 162, 60, 0.1);
        color: #e6a23c;
      }

      &.red {
        background: rgba(245, 108, 108, 0.1);
        color: #f56c6c;
      }
    }

    .stat-info {
      h3 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
      }

      p {
        margin: 4px 0 0;
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
