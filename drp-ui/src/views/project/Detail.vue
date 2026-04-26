<template>
  <div class="page-container">
    <el-page-header @back="() => $router.back()" :content="projectName" />

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ memberCount }}</h3>
          <p>项目成员</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon><Key /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ variableCount }}</h3>
          <p>环境变量</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon><Connection /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ policyCount }}</h3>
          <p>分支策略</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon><Promotion /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ deployServerCount }}</h3>
          <p>部署服务器</p>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="card">
      <div class="tab-nav">
        <div class="tab-item" :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'">
          <el-icon><InfoFilled /></el-icon> 基本信息
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'members' }" @click="activeTab = 'members'">
          <el-icon><UserFilled /></el-icon> 项目成员
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'variables' }" @click="activeTab = 'variables'">
          <el-icon><Key /></el-icon> 环境变量
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'servers' }" @click="activeTab = 'servers'">
          <el-icon><Promotion /></el-icon> 部署服务器
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'policies' }" @click="activeTab = 'policies'">
          <el-icon><Connection /></el-icon> 分支策略
        </div>
      </div>

      <!-- 基本信息 Tab -->
      <div v-show="activeTab === 'basic'" class="tab-content">
        <div class="card-header">
          <div class="card-title"><el-icon><InfoFilled /></el-icon> 项目基本信息</div>
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon> 编辑配置
          </el-button>
        </div>
        <div class="info-grid" v-if="projectDetail">
          <div class="info-item">
            <div class="info-label">项目名称</div>
            <div class="info-value">{{ projectDetail.name }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">项目编码</div>
            <div class="info-value">{{ projectDetail.code }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">项目类型</div>
            <div class="info-value">
              <span class="status-tag status-info">{{ projectDetail.typeDesc }}</span>
            </div>
          </div>
          <div class="info-item">
            <div class="info-label">状态</div>
            <div class="info-value">
              <span class="status-dot" :class="projectDetail.status === 1 ? 'online' : 'offline'"></span>
              {{ projectDetail.statusDesc }}
            </div>
          </div>
          <div class="info-item full-width">
            <div class="info-label">项目描述</div>
            <div class="info-value">{{ projectDetail.description || '-' }}</div>
          </div>
        </div>

        <el-divider />

        <div class="card-header">
          <div class="card-title"><el-icon><Operation /></el-icon> Git 与构建配置</div>
        </div>
        <div class="info-grid" v-if="projectDetail">
          <div class="info-item full-width">
            <div class="info-label">Git 仓库地址</div>
            <div class="info-value" style="word-break: break-all; color: var(--primary-color)">
              {{ projectDetail.gitUrl }}
            </div>
          </div>
          <div class="info-item">
            <div class="info-label">关联凭证</div>
            <div class="info-value">{{ projectDetail.credentialName || '未关联' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">默认分支</div>
            <div class="info-value">{{ projectDetail.defaultBranch }}</div>
          </div>
          <div class="info-item full-width">
            <div class="info-label">构建配置</div>
            <div class="info-value">
              <code v-if="projectDetail.buildConfig" class="config-code">{{ projectDetail.buildConfig }}</code>
              <span v-else>-</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 部署服务器 Tab -->
      <div v-show="activeTab === 'servers'" class="tab-content">
        <div class="card-header">
          <div class="card-title"><el-icon><Promotion /></el-icon> 部署服务器绑定</div>
          <div style="display: flex; gap: 8px">
            <el-select v-model="deployEnvFilter" placeholder="按环境筛选" clearable style="width: 140px" @change="loadDeployServers">
              <el-option label="开发环境" value="dev" />
              <el-option label="测试环境" value="test" />
              <el-option label="预发环境" value="pre" />
              <el-option label="生产环境" value="prod" />
            </el-select>
            <el-button type="primary" @click="handleCreateDeployServer">
              <el-icon><Plus /></el-icon> 绑定服务器
            </el-button>
          </div>
        </div>
        <el-table v-loading="deployServerLoading" :data="deployServerList" stripe>
          <el-table-column prop="envName" label="部署环境" width="120">
            <template #default="{ row }">
              <span class="status-tag" :class="getEnvClass(row.env)">{{ row.envName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="serverName" label="服务器" min-width="180" />
          <el-table-column label="主机地址" min-width="180">
            <template #default="{ row }">
              {{ row.hostname }}<span v-if="row.port">:{{ row.port }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="serverStatusDesc" label="服务器状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getServerStatusType(row.serverStatus)">{{ row.serverStatusDesc || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deployPath" label="部署路径" min-width="220" show-overflow-tooltip />
          <el-table-column prop="createTime" label="绑定时间" width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEditDeployServer(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDeleteDeployServer(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 项目成员 Tab -->
      <div v-show="activeTab === 'members'" class="tab-content">
        <div class="card-header">
          <div class="card-title"><el-icon><UserFilled /></el-icon> 项目成员</div>
          <el-button type="primary" @click="showMemberDialog = true">
            <el-icon><Plus /></el-icon> 添加成员
          </el-button>
        </div>
        <el-table v-loading="memberLoading" :data="memberList" stripe>
          <el-table-column label="成员" min-width="180">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 10px">
                <el-avatar :size="32" :style="{ background: getAvatarColor(row.userId) }">
                  {{ row.realName?.charAt(0) || 'U' }}
                </el-avatar>
                <div>
                  <div style="font-weight: 500">{{ row.realName }}</div>
                  <div style="font-size: 12px; color: #909399">{{ row.username }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="roleDesc" label="角色" width="140">
            <template #default="{ row }">
              <span class="status-tag" :class="getRoleClass(row.role)">{{ row.roleDesc }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="permission" label="权限说明" min-width="220" />
          <el-table-column prop="createTime" label="加入时间" width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEditMember(row)">修改角色</el-button>
              <el-button
                v-if="row.role !== 'OWNER'"
                type="danger"
                link
                @click="handleRemoveMember(row)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 环境变量 Tab -->
      <div v-show="activeTab === 'variables'" class="tab-content">
        <div class="card-header">
          <div class="card-title"><el-icon><Key /></el-icon> 环境变量</div>
          <div style="display: flex; gap: 8px">
            <el-select v-model="filterEnv" placeholder="按环境筛选" clearable style="width: 120px" @change="loadVariables">
              <el-option label="开发环境" value="dev" />
              <el-option label="测试环境" value="test" />
              <el-option label="生产环境" value="prod" />
            </el-select>
            <el-button type="primary" @click="showVariableDialog = true">
              <el-icon><Plus /></el-icon> 新增变量
            </el-button>
          </div>
        </div>
        <el-table v-loading="variableLoading" :data="variableList" stripe>
          <el-table-column prop="envName" label="环境" width="120">
            <template #default="{ row }">
              <span class="status-tag" :class="getEnvClass(row.envCode)">{{ row.envName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="varKey" label="变量名" min-width="180">
            <template #default="{ row }">
              <span style="font-family: monospace; font-weight: 500">{{ row.varKey }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="varValue" label="变量值" min-width="200">
            <template #default="{ row }">
              <span v-if="row.isSecret == 1" style="color: #909399">********</span>
              <span v-else style="font-family: monospace">{{ row.varValue }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="isSecretDesc" label="脱敏" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isSecret === 1" type="warning" size="small">是</el-tag>
              <el-tag v-else type="info" size="small">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEditVariable(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDeleteVariable(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分支策略 Tab -->
      <div v-show="activeTab === 'policies'" class="tab-content">
        <div class="card-header">
          <div class="card-title"><el-icon><Connection /></el-icon> 分支策略</div>
          <el-button type="primary" @click="showPolicyDialog = true">
            <el-icon><Plus /></el-icon> 新增策略
          </el-button>
        </div>
        <el-table v-loading="policyLoading" :data="policyList" stripe>
          <el-table-column prop="branchPattern" label="分支匹配模式" min-width="200">
            <template #default="{ row }">
              <code class="pattern-code">{{ row.branchPattern }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="allowAutoDeployDesc" label="自动部署" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.allowAutoDeploy === 1" type="success" size="small">允许</el-tag>
              <el-tag v-else type="info" size="small">禁止</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="requireApprovalDesc" label="需要审批" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.requireApproval === 1" type="warning" size="small">是</el-tag>
              <el-tag v-else type="info" size="small">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEditPolicy(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDeletePolicy(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="showMemberDialog" title="添加项目成员" width="500px" destroy-on-close>
      <el-form :model="memberForm" label-width="100px">
        <el-form-item label="选择用户" required>
          <el-select v-model="memberForm.userId" placeholder="请选择用户" filterable style="width: 100%">
            <el-option v-for="user in availableUsers" :key="user.id" :label="`${user.username} - ${user.realName}`" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目角色" required>
          <el-radio-group v-model="memberForm.role">
            <el-radio label="OWNER">所有者 (OWNER)</el-radio>
            <el-radio label="DEVELOPER">开发者 (DEVELOPER)</el-radio>
            <el-radio label="REPORTER">观察者 (REPORTER)</el-radio>
          </el-radio-group>
        </el-form-item>
        <div style="padding: 12px 16px; background: #f5f7fa; border-radius: 8px; font-size: 13px; color: #606266; line-height: 1.8">
          <div style="font-weight: 500; margin-bottom: 4px">角色权限说明：</div>
          <ul style="margin: 0; padding-left: 18px">
            <li><b>所有者</b>：项目配置、成员与策略管理</li>
            <li><b>开发者</b>：可维护环境变量并触发测试环境发布</li>
            <li><b>观察者</b>：只读访问，不能修改配置</li>
          </ul>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showMemberDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember" :loading="submitLoading">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 修改角色对话框 -->
    <el-dialog v-model="showRoleDialog" title="修改成员角色" width="450px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="当前成员">
          <div style="display: flex; align-items: center; gap: 10px">
            <el-avatar :size="32" :style="{ background: getAvatarColor(currentMember?.userId) }">
              {{ currentMember?.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <span>{{ currentMember?.realName }}</span>
          </div>
        </el-form-item>
        <el-form-item label="项目角色" required>
          <el-radio-group v-model="memberForm.role">
            <el-radio label="OWNER">所有者 (OWNER)</el-radio>
            <el-radio label="DEVELOPER">开发者 (DEVELOPER)</el-radio>
            <el-radio label="REPORTER">观察者 (REPORTER)</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRoleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateRole" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 环境变量对话框 -->
    <el-dialog v-model="showVariableDialog" :title="editingVariable ? '编辑环境变量' : '新增环境变量'" width="500px" destroy-on-close>
      <el-form :model="variableForm" label-width="100px">
        <el-form-item label="环境" required>
          <el-select v-model="variableForm.envCode" placeholder="请选择环境" style="width: 100%">
            <el-option label="开发环境 (dev)" value="dev" />
            <el-option label="测试环境 (test)" value="test" />
            <el-option label="生产环境 (prod)" value="prod" />
          </el-select>
        </el-form-item>
        <el-form-item label="变量名" required>
          <el-input v-model="variableForm.varKey" placeholder="如: DATABASE_URL" />
        </el-form-item>
        <el-form-item label="变量值" required>
          <el-input v-model="variableForm.varValue" placeholder="请输入变量值" />
        </el-form-item>
        <el-form-item label="脱敏显示">
          <el-switch v-model="variableForm.isSecret" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 12px; color: #909399; font-size: 12px">开启后变量值将显示为 ********</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVariableDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveVariable" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 部署服务器对话框 -->
    <el-dialog v-model="showDeployServerDialog" :title="editingDeployServer ? '编辑部署服务器' : '绑定部署服务器'" width="560px" destroy-on-close>
      <el-form :model="deployServerForm" label-width="100px">
        <el-form-item label="部署环境" required>
          <el-select v-model="deployServerForm.env" placeholder="请选择环境" style="width: 100%">
            <el-option label="开发环境 (dev)" value="dev" />
            <el-option label="测试环境 (test)" value="test" />
            <el-option label="预发环境 (pre)" value="pre" />
            <el-option label="生产环境 (prod)" value="prod" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务器" required>
          <el-select v-model="deployServerForm.serverId" placeholder="请选择服务器" filterable style="width: 100%">
            <el-option
              v-for="server in serverOptions"
              :key="server.id"
              :label="`${server.name} (${server.hostname})`"
              :value="server.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部署路径" required>
          <el-input v-model="deployServerForm.deployPath" placeholder="如: /opt/apps/aliveApp" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeployServerDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDeployServer" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分支策略对话框 -->
    <el-dialog v-model="showPolicyDialog" :title="editingPolicy ? '编辑分支策略' : '新增分支策略'" width="550px" destroy-on-close>
      <el-form :model="policyForm" label-width="120px">
        <el-form-item label="分支匹配模式" required>
          <el-input v-model="policyForm.branchPattern" placeholder="如: release/* 或 hotfix/*" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px">支持通配符 * 匹配任意字符，如 release/* 匹配 release/v1.0</div>
        </el-form-item>
        <el-form-item label="自动部署">
          <el-switch v-model="policyForm.allowAutoDeploy" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 12px; color: #909399; font-size: 12px">匹配的分支是否允许自动触发部署</span>
        </el-form-item>
        <el-form-item label="需要人工审批">
          <el-switch v-model="policyForm.requireApproval" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 12px; color: #909399; font-size: 12px">部署前是否需要人工审批确认</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPolicyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSavePolicy" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProjectDetail,
  getProjectMembers,
  addProjectMember,
  updateProjectMember,
  removeProjectMember,
  getAvailableUsers,
  getEnvVariables,
  createEnvVariable,
  updateEnvVariable,
  deleteEnvVariable,
  getProjectDeployServers,
  createProjectDeployServer,
  updateProjectDeployServer,
  deleteProjectDeployServer,
  getBranchPolicies,
  createBranchPolicy,
  updateBranchPolicy,
  deleteBranchPolicy,
  type Project,
  type ProjectMember,
  type MemberRole,
  type EnvVariable,
  type ProjectDeployServer,
  type BranchPolicy
} from '@/api/project'
import { getServerAll, type Server } from '@/api/server'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(false)
const memberLoading = ref(false)
const submitLoading = ref(false)
const activeTab = ref('basic')
const filterEnv = ref('')

// 数据
const projectDetail = ref<Project | null>(null)
const memberList = ref<ProjectMember[]>([])
const availableUsers = ref<{ id: number; username: string; realName: string }[]>([])
const serverOptions = ref<Server[]>([])

// 统计数据
const memberCount = computed(() => memberList.value.length)
const variableCount = ref(0)
const policyCount = ref(0)
const deployServerCount = ref(0)
const projectName = computed(() => projectDetail.value?.name || '项目详情')

// 对话框状态
const showMemberDialog = ref(false)
const showRoleDialog = ref(false)
const showVariableDialog = ref(false)
const showDeployServerDialog = ref(false)
const showPolicyDialog = ref(false)
const currentMember = ref<ProjectMember | null>(null)

// 环境变量相关
const variableLoading = ref(false)
const variableList = ref<EnvVariable[]>([])
const editingVariable = ref<EnvVariable | null>(null)
const variableForm = reactive({
  envCode: 'dev' as 'dev' | 'test' | 'prod',
  varKey: '',
  varValue: '',
  isSecret: 0
})

// 部署服务器相关
const deployServerLoading = ref(false)
const deployServerList = ref<ProjectDeployServer[]>([])
const deployEnvFilter = ref('')
const editingDeployServer = ref<ProjectDeployServer | null>(null)
const deployServerForm = reactive({
  serverId: undefined as number | undefined,
  env: 'test' as 'dev' | 'test' | 'pre' | 'prod',
  deployPath: ''
})

// 分支策略相关
const policyLoading = ref(false)
const policyList = ref<BranchPolicy[]>([])
const editingPolicy = ref<BranchPolicy | null>(null)
const policyForm = reactive({
  branchPattern: '',
  allowAutoDeploy: 0,
  requireApproval: 0
})

// 成员表单
const memberForm = reactive({
  userId: undefined as number | undefined,
  role: 'DEVELOPER' as MemberRole
})

// 获取项目详情
async function loadProjectDetail() {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('无效的项目ID')
    router.back()
    return
  }

  loading.value = true
  try {
    projectDetail.value = await getProjectDetail(id)
  } catch (error) {
    console.error('加载项目详情失败:', error)
    ElMessage.error('加载项目详情失败')
  } finally {
    loading.value = false
  }
}

// 获取项目成员列表
async function loadMembers() {
  const id = Number(route.params.id)
  if (!id) return

  memberLoading.value = true
  try {
    memberList.value = await getProjectMembers(id)
  } catch (error) {
    console.error('加载成员列表失败:', error)
  } finally {
    memberLoading.value = false
  }
}

// 获取可选用户列表
async function loadAvailableUsers() {
  try {
    availableUsers.value = await getAvailableUsers()
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

async function loadServerOptions() {
  try {
    serverOptions.value = await getServerAll()
  } catch (error) {
    console.error('加载服务器列表失败:', error)
  }
}

// 监听添加成员对话框打开
watch(showMemberDialog, (val) => {
  if (val) {
    loadAvailableUsers()
  }
})

// 添加成员
async function handleAddMember() {
  if (!memberForm.userId) {
    ElMessage.warning('请选择用户')
    return
  }

  const id = Number(route.params.id)
  submitLoading.value = true
  try {
    await addProjectMember(id, { userId: memberForm.userId, role: memberForm.role })
    ElMessage.success('添加成功')
    showMemberDialog.value = false
    memberForm.userId = undefined
    memberForm.role = 'DEVELOPER'
    loadMembers()
  } catch (error: any) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    submitLoading.value = false
  }
}

// 编辑成员角色
function handleEditMember(row: ProjectMember) {
  currentMember.value = row
  memberForm.role = row.role as MemberRole
  showRoleDialog.value = true
}

// 更新成员角色
async function handleUpdateRole() {
  if (!currentMember.value) return

  const id = Number(route.params.id)
  submitLoading.value = true
  try {
    await updateProjectMember(id, currentMember.value.userId, { role: memberForm.role })
    ElMessage.success('更新成功')
    showRoleDialog.value = false
    loadMembers()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    submitLoading.value = false
  }
}

// 移除成员
async function handleRemoveMember(row: ProjectMember) {
  try {
    await ElMessageBox.confirm(`确定要移除成员「${row.realName}」吗？`, '提示', { type: 'warning' })
    const id = Number(route.params.id)
    await removeProjectMember(id, row.userId)
    ElMessage.success('移除成功')
    loadMembers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
    }
  }
}

// 编辑项目
function handleEdit() {
  router.push({ name: 'ProjectList' })
}

// ============ 环境变量相关 ============

async function loadVariables() {
  const id = Number(route.params.id)
  if (!id) return

  variableLoading.value = true
  try {
    variableList.value = await getEnvVariables(id, filterEnv.value || undefined)
    variableCount.value = variableList.value.length
  } catch (error) {
    console.error('加载环境变量失败:', error)
  } finally {
    variableLoading.value = false
  }
}

function handleEditVariable(row: EnvVariable) {
  editingVariable.value = row
  variableForm.envCode = row.envCode as 'dev' | 'test' | 'prod'
  variableForm.varKey = row.varKey
  variableForm.varValue = row.varValue
  variableForm.isSecret = row.isSecret
  showVariableDialog.value = true
}

async function handleSaveVariable() {
  if (!variableForm.varKey || !variableForm.varValue) {
    ElMessage.warning('请填写完整的变量信息')
    return
  }

  const id = Number(route.params.id)
  submitLoading.value = true
  try {
    if (editingVariable.value) {
      await updateEnvVariable(id, editingVariable.value.id, {
        envCode: variableForm.envCode,
        varKey: variableForm.varKey,
        varValue: variableForm.varValue,
        isSecret: variableForm.isSecret
      })
      ElMessage.success('更新成功')
    } else {
      await createEnvVariable(id, {
        envCode: variableForm.envCode,
        varKey: variableForm.varKey,
        varValue: variableForm.varValue,
        isSecret: variableForm.isSecret
      })
      ElMessage.success('创建成功')
    }
    showVariableDialog.value = false
    editingVariable.value = null
    resetVariableForm()
    loadVariables()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDeleteVariable(row: EnvVariable) {
  try {
    await ElMessageBox.confirm(`确定要删除变量「${row.varKey}」吗？`, '提示', { type: 'warning' })
    const id = Number(route.params.id)
    await deleteEnvVariable(id, row.id)
    ElMessage.success('删除成功')
    loadVariables()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除变量失败:', error)
    }
  }
}

function resetVariableForm() {
  variableForm.envCode = 'dev'
  variableForm.varKey = ''
  variableForm.varValue = ''
  variableForm.isSecret = 0
}

// ============ 部署服务器相关 ============

async function loadDeployServers() {
  const id = Number(route.params.id)
  if (!id) return

  deployServerLoading.value = true
  try {
    deployServerList.value = await getProjectDeployServers(id, deployEnvFilter.value || undefined)
    deployServerCount.value = deployServerList.value.length
  } catch (error) {
    console.error('加载部署服务器失败:', error)
  } finally {
    deployServerLoading.value = false
  }
}

function handleCreateDeployServer() {
  editingDeployServer.value = null
  resetDeployServerForm()
  showDeployServerDialog.value = true
  if (!serverOptions.value.length) {
    loadServerOptions()
  }
}

function handleEditDeployServer(row: ProjectDeployServer) {
  editingDeployServer.value = row
  deployServerForm.serverId = row.serverId
  deployServerForm.env = row.env
  deployServerForm.deployPath = row.deployPath
  showDeployServerDialog.value = true
  if (!serverOptions.value.length) {
    loadServerOptions()
  }
}

async function handleSaveDeployServer() {
  if (!deployServerForm.serverId || !deployServerForm.deployPath) {
    ElMessage.warning('请填写完整的部署服务器信息')
    return
  }

  const id = Number(route.params.id)
  submitLoading.value = true
  try {
    const payload = {
      serverId: deployServerForm.serverId,
      env: deployServerForm.env,
      deployPath: deployServerForm.deployPath
    }
    if (editingDeployServer.value) {
      await updateProjectDeployServer(id, editingDeployServer.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createProjectDeployServer(id, payload)
      ElMessage.success('绑定成功')
    }
    showDeployServerDialog.value = false
    editingDeployServer.value = null
    resetDeployServerForm()
    loadDeployServers()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDeleteDeployServer(row: ProjectDeployServer) {
  try {
    await ElMessageBox.confirm(`确定要删除服务器绑定「${row.serverName} / ${row.envName}」吗？`, '提示', { type: 'warning' })
    const id = Number(route.params.id)
    await deleteProjectDeployServer(id, row.id)
    ElMessage.success('删除成功')
    loadDeployServers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除部署服务器失败:', error)
    }
  }
}

function resetDeployServerForm() {
  deployServerForm.serverId = undefined
  deployServerForm.env = 'test'
  deployServerForm.deployPath = ''
}

// ============ 分支策略相关 ============

async function loadPolicies() {
  const id = Number(route.params.id)
  if (!id) return

  policyLoading.value = true
  try {
    policyList.value = await getBranchPolicies(id)
    policyCount.value = policyList.value.length
  } catch (error) {
    console.error('加载分支策略失败:', error)
  } finally {
    policyLoading.value = false
  }
}

function handleEditPolicy(row: BranchPolicy) {
  editingPolicy.value = row
  policyForm.branchPattern = row.branchPattern
  policyForm.allowAutoDeploy = row.allowAutoDeploy
  policyForm.requireApproval = row.requireApproval
  showPolicyDialog.value = true
}

async function handleSavePolicy() {
  if (!policyForm.branchPattern) {
    ElMessage.warning('请填写分支匹配模式')
    return
  }

  const id = Number(route.params.id)
  submitLoading.value = true
  try {
    if (editingPolicy.value) {
      await updateBranchPolicy(id, editingPolicy.value.id, {
        branchPattern: policyForm.branchPattern,
        allowAutoDeploy: policyForm.allowAutoDeploy,
        requireApproval: policyForm.requireApproval
      })
      ElMessage.success('更新成功')
    } else {
      await createBranchPolicy(id, {
        branchPattern: policyForm.branchPattern,
        allowAutoDeploy: policyForm.allowAutoDeploy,
        requireApproval: policyForm.requireApproval
      })
      ElMessage.success('创建成功')
    }
    showPolicyDialog.value = false
    editingPolicy.value = null
    resetPolicyForm()
    loadPolicies()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDeletePolicy(row: BranchPolicy) {
  try {
    await ElMessageBox.confirm(`确定要删除策略「${row.branchPattern}」吗？`, '提示', { type: 'warning' })
    const id = Number(route.params.id)
    await deleteBranchPolicy(id, row.id)
    ElMessage.success('删除成功')
    loadPolicies()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除策略失败:', error)
    }
  }
}

function resetPolicyForm() {
  policyForm.branchPattern = ''
  policyForm.allowAutoDeploy = 0
  policyForm.requireApproval = 0
}

// 监听 Tab 切换
watch(activeTab, (tab) => {
  if (tab === 'variables') {
    loadVariables()
  } else if (tab === 'servers') {
    loadDeployServers()
    if (!serverOptions.value.length) {
      loadServerOptions()
    }
  } else if (tab === 'policies') {
    loadPolicies()
  }
})

// 工具函数：获取角色样式
function getRoleClass(role: string) {
  const map: Record<string, string> = {
    OWNER: 'status-danger',
    DEVELOPER: 'status-success',
    REPORTER: 'status-info'
  }
  return map[role] || 'status-info'
}

// 工具函数：获取环境样式
function getEnvClass(envCode: string) {
  const map: Record<string, string> = {
    dev: 'status-success',
    test: 'status-warning',
    pre: 'status-info',
    prod: 'status-danger'
  }
  return map[envCode] || 'status-info'
}

function getServerStatusType(status?: number) {
  const map: Record<number, string> = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return status != null ? map[status] || 'info' : 'info'
}

// 工具函数：获取头像颜色
function getAvatarColor(userId?: number) {
  const colors = [
    'linear-gradient(135deg, #667eea, #764ba2)',
    'linear-gradient(135deg, #409eff, #67c23a)',
    'linear-gradient(135deg, #e6a23c, #f7ba2a)',
    'linear-gradient(135deg, #f56c6c, #fab6b6)',
    'linear-gradient(135deg, #909399, #b1b3b8)'
  ]
  return colors[(userId ?? 0) % colors.length]
}

onMounted(() => {
  loadProjectDetail()
  loadMembers()
  loadDeployServers()
})
</script>

<style scoped>
.page-container {
  animation: fadeIn 0.3s ease;
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.stat-icon.green {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
}

.stat-info h3 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-info p {
  color: #909399;
  font-size: 13px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Tab 样式 */
.tab-nav {
  display: flex;
  gap: 4px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 24px;
}

.tab-item {
  padding: 12px 20px;
  cursor: pointer;
  color: #606266;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.3s;
}

.tab-item:hover {
  color: var(--primary-color);
}

.tab-item.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.tab-content {
  min-height: 300px;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  padding: 12px 0;
}

.info-item.full-width {
  grid-column: span 2;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.config-code {
  display: block;
  padding: 12px 16px;
  background: #1f2937;
  color: #e5e7eb;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-danger {
  background: #fef0f0;
  color: #f56c6c;
}

.status-success {
  background: #f0f9eb;
  color: #67c23a;
}

.status-info {
  background: #ecf5ff;
  color: #409eff;
}

.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.pattern-code {
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: monospace;
  color: #606266;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-dot.online {
  background: #67c23a;
}

.status-dot.offline {
  background: #909399;
}
</style>
