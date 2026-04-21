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
            <el-select v-model="filterEnv" placeholder="按环境筛选" clearable style="width: 120px">
              <el-option label="开发环境" value="dev" />
              <el-option label="测试环境" value="test" />
              <el-option label="生产环境" value="prod" />
            </el-select>
            <el-button type="primary">
              <el-icon><Plus /></el-icon> 新增变量
            </el-button>
          </div>
        </div>
        <el-empty description="环境变量功能开发中" />
      </div>

      <!-- 分支策略 Tab -->
      <div v-show="activeTab === 'policies'" class="tab-content">
        <div class="card-header">
          <div class="card-title"><el-icon><Connection /></el-icon> 分支策略</div>
          <el-button type="primary">
            <el-icon><Plus /></el-icon> 新增策略
          </el-button>
        </div>
        <el-empty description="分支策略功能开发中" />
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
  type Project,
  type ProjectMember,
  type MemberRole
} from '@/api/project'

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

// 统计数据
const memberCount = computed(() => memberList.value.length)
const variableCount = ref(0)
const policyCount = ref(0)
const projectName = computed(() => projectDetail.value?.name || '项目详情')

// 对话框状态
const showMemberDialog = ref(false)
const showRoleDialog = ref(false)
const currentMember = ref<ProjectMember | null>(null)

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

// 工具函数：获取角色样式
function getRoleClass(role: string) {
  const map: Record<string, string> = {
    OWNER: 'status-danger',
    DEVELOPER: 'status-success',
    REPORTER: 'status-info'
  }
  return map[role] || 'status-info'
}

// 工具函数：获取头像颜色
function getAvatarColor(userId: number) {
  const colors = [
    'linear-gradient(135deg, #667eea, #764ba2)',
    'linear-gradient(135deg, #409eff, #67c23a)',
    'linear-gradient(135deg, #e6a23c, #f7ba2a)',
    'linear-gradient(135deg, #f56c6c, #fab6b6)',
    'linear-gradient(135deg, #909399, #b1b3b8)'
  ]
  return colors[userId % colors.length]
}

onMounted(() => {
  loadProjectDetail()
  loadMembers()
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
