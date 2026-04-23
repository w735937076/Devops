<template>
  <div class="page-container">
    <!-- 项目列表页面 -->
    <template v-if="!dialogVisible">
      <div class="page-header">
        <h2 class="page-title">项目管理</h2>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="stat-info">
            <h3>{{ total }}</h3>
            <p>项目总数</p>
          </div>
        </div>
        <div class="stat-card" style="cursor: pointer" @click="$router.push('/credentials')">
          <div class="stat-icon green">
            <el-icon><Key /></el-icon>
          </div>
          <div class="stat-info">
            <h3>{{ credentialCount }}</h3>
            <p>可用凭证</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Share /></el-icon>
          </div>
          <div class="stat-info">
            <h3>0</h3>
            <p>分支策略数</p>
          </div>
        </div>
      </div>

      <!-- 搜索筛选区域 -->
      <div class="card">
        <div class="card-header" style="align-items: flex-start">
          <div>
            <div class="card-title">
              <el-icon><FolderOpened /></el-icon> 项目列表
            </div>
            <div style="font-size: 13px; color: #909399; margin-top: 6px">
              支持按关键字、类型、状态筛选，快速进入项目详情配置成员、变量与分支规则。
            </div>
          </div>
          <div style="display: flex; gap: 12px; flex-wrap: wrap; justify-content: flex-end">
            <el-input
              v-model="queryParams.keyword"
              placeholder="搜索项目名称/编码..."
              style="width: 220px"
              clearable
              @keyup.enter="handleQuery"
            >
              <template #prefix><i class="el-input__icon el-icon-search"></i></template>
            </el-input>
            <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 140px">
              <el-option label="Java Maven" value="JAVA_MAVEN" />
              <el-option label="Node.js" value="NODE" />
              <el-option label="Python" value="PYTHON" />
            </el-select>
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon> 创建项目
            </el-button>
          </div>
        </div>

        <!-- 表格 -->
        <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
          <el-table-column prop="name" label="项目名称" min-width="220">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 10px">
                <div
                  style="
                    width: 36px;
                    height: 36px;
                    border-radius: 8px;
                    background: linear-gradient(135deg, #667eea, #764ba2);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-weight: 600;
                  "
                >
                  {{ row.name.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <div style="font-weight: 500">{{ row.name }}</div>
                  <div style="font-size: 12px; color: #909399">编码：{{ row.code }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="项目类型" width="130">
            <template #default="{ row }">
              <span class="status-tag status-info">{{ row.typeDesc }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="gitUrl" label="Git 仓库" min-width="220">
            <template #default="{ row }">
              <div style="font-size: 13px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">
                {{ row.gitUrl }}
              </div>
              <div style="font-size: 12px; color: #909399">默认分支：{{ row.defaultBranch }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="credentialName" label="关联凭证" width="180">
            <template #default="{ row }">
              <div v-if="row.credentialName" style="display: flex; align-items: center; gap: 8px">
                <el-icon style="color: #e6a23c"><Key /></el-icon>
                <span>{{ row.credentialName }}</span>
              </div>
              <span v-else style="color: #c0c4cc">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="statusDesc" label="状态" width="100">
            <template #default="{ row }">
              <div class="server-status">
                <span class="status-dot" :class="row.status === 1 ? 'online' : 'offline'"></span>
                {{ row.statusDesc }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="最近更新" width="170" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-tooltip content="详情" placement="top">
                  <el-button type="primary" circle @click="$router.push(`/projects/${row.id}`)">
                    <el-icon><Document /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="编辑" placement="top">
                  <el-button type="primary" circle @click="handleEdit(row)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button type="danger" circle @click="handleDelete(row)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 20px; display: flex; justify-content: center">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </template>

    <!-- 创建/编辑项目对话框 - 分步向导 -->
    <template v-else>
      <div class="page-header">
        <el-page-header @back="handleBack" :content="dialogTitle" />
      </div>

      <!-- 步骤条 -->
      <div class="steps">
        <div class="step" :class="{ active: currentStep === 1, completed: currentStep > 1 }">
          <div class="step-number">
            <el-icon v-if="currentStep > 1"><Check /></el-icon>
            <span v-else>1</span>
          </div>
          <div class="step-label">基本信息</div>
        </div>
        <div class="step" :class="{ active: currentStep === 2, completed: currentStep > 2 }">
          <div class="step-number">
            <el-icon v-if="currentStep > 2"><Check /></el-icon>
            <span v-else>2</span>
          </div>
          <div class="step-label">Git配置</div>
        </div>
        <div class="step" :class="{ active: currentStep === 3, completed: currentStep > 3 }">
          <div class="step-number">
            <el-icon v-if="currentStep > 3"><Check /></el-icon>
            <span v-else>3</span>
          </div>
          <div class="step-label">构建配置</div>
        </div>
        <div class="step" :class="{ active: currentStep === 4 }">
          <div class="step-number">4</div>
          <div class="step-label">完成确认</div>
        </div>
      </div>

      <div class="grid-2">
        <!-- 左侧：表单区域 -->
        <div class="card">
          <!-- Step 1: 基本信息 -->
          <div v-show="currentStep === 1">
            <div class="card-header">
              <div class="card-title">
                <el-icon style="color: var(--primary-color)"><Operation /></el-icon> Step 1 - 基本信息
              </div>
            </div>
            <div style="max-width: 620px">
              <div class="form-group">
                <label class="form-label">项目名称 <span style="color: #f56c6c">*</span></label>
                <el-input v-model="formData.name" placeholder="请输入项目名称" />
              </div>
              <div class="form-group">
                <label class="form-label">项目编码 <span style="color: #f56c6c">*</span></label>
                <el-input
                  v-model="formData.code"
                  placeholder="如 drp-api，仅支持小写字母/数字/下划线"
                  :disabled="isEdit"
                />
                <div style="font-size: 12px; color: #909399; margin-top: 6px">
                  用于 Linux 目录名，全局唯一，提交时会校验编码是否重复。
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">项目类型 <span style="color: #f56c6c">*</span></label>
                <div style="display: flex; gap: 12px; flex-wrap: wrap">
                  <label
                    class="type-radio"
                    :class="{ active: formData.type === 'JAVA_MAVEN' }"
                  >
                    <input type="radio" v-model="formData.type" value="JAVA_MAVEN" /> JAVA_MAVEN
                  </label>
                  <label class="type-radio" :class="{ active: formData.type === 'NODE' }">
                    <input type="radio" v-model="formData.type" value="NODE" /> NODE
                  </label>
                  <label class="type-radio" :class="{ active: formData.type === 'PYTHON' }">
                    <input type="radio" v-model="formData.type" value="PYTHON" /> PYTHON
                  </label>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">项目描述</label>
                <el-input
                  v-model="formData.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入项目描述"
                />
              </div>
            </div>
          </div>

          <!-- Step 2: Git配置 -->
          <div v-show="currentStep === 2">
            <div class="card-header">
              <div class="card-title">
                <i class="fab fa-git-alt" style="color: #f05032"></i> Step 2 - Git 配置
              </div>
            </div>
            <div style="max-width: 620px">
              <div class="form-group">
                <label class="form-label">Git 仓库地址 <span style="color: #f56c6c">*</span></label>
                <el-input v-model="formData.gitUrl" placeholder="https://gitlab.example.com/group/project.git" />
              </div>
              <div class="form-group">
                <label class="form-label">关联凭证</label>
                <el-select v-model="formData.credentialId" placeholder="请选择凭证（选填）" clearable class="w-full">
                  <el-option v-for="cred in credentialList" :key="cred.id" :label="cred.name + '（' + cred.typeDesc + '）'" :value="cred.id" />
                </el-select>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px">
                  <span style="font-size: 12px; color: #909399">凭证统一由凭证中心维护，详情接口仅返回脱敏信息。</span>
                  <el-button type="primary" link size="small" @click="$router.push('/credentials')">+ 快速新建凭证</el-button>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">默认分支</label>
                <div style="display: flex; gap: 8px; align-items: center">
                  <el-select
                    v-model="formData.defaultBranch"
                    placeholder="请选择或输入分支名"
                    filterable
                    allow-create
                    default-first-option
                    style="flex: 1"
                  >
                    <el-option v-for="branch in branchList" :key="branch" :label="branch" :value="branch" />
                  </el-select>
                  <el-button
                    @click="handleFetchBranches"
                    :loading="fetchingBranches"
                    :disabled="!formData.gitUrl"
                  >
                    拉取分支
                  </el-button>
                </div>
              </div>
              <div style="padding: 14px 16px; background: #f5f7fa; border-radius: 10px">
                <div style="font-weight: 500; margin-bottom: 8px">凭证使用说明</div>
                <div style="font-size: 13px; color: #606266; line-height: 1.8">
                  支持 `USERNAME_PASSWORD`、`ACCESS_TOKEN`、`SSH_KEY` 三种类型；项目中仅存储
                  `credential_id`，敏感内容由凭证中心统一加密保存。
                </div>
              </div>
            </div>
          </div>

          <!-- Step 3: 构建配置 -->
          <div v-show="currentStep === 3">
            <div class="card-header">
              <div class="card-title">
                <el-icon style="color: var(--primary-color)"><Setting /></el-icon> Step 3 - 构建配置
              </div>
            </div>
            <div style="padding: 16px; background: #f8faff; border: 1px dashed #d9ecff; border-radius: 12px; margin-bottom: 16px">
              <div style="font-weight: 500; margin-bottom: 6px">当前项目类型：{{ formData.type }}</div>
              <div style="font-size: 13px; color: #909399">
                根据项目类型动态展示差异化配置，最终以 `build_config` JSON 保存。
              </div>
            </div>

            <!-- JAVA_MAVEN 构建配置 -->
            <div v-if="formData.type === 'JAVA_MAVEN'" style="max-width: 620px">
              <div class="form-group">
                <label class="form-label">JDK 版本</label>
                <el-input v-model="buildConfig.jdk" placeholder="如 17" />
              </div>
              <div class="form-group">
                <label class="form-label">Maven 版本</label>
                <el-input v-model="buildConfig.maven" placeholder="如 3.8.6" />
              </div>
              <div class="form-group">
                <label class="form-label">构建命令</label>
                <el-input v-model="buildConfig.goals" placeholder="如 clean package -DskipTests" />
              </div>
              <div class="form-group">
                <label class="form-label">Dockerfile 路径</label>
                <el-input v-model="buildConfig.dockerfile" placeholder="如 Dockerfile" />
              </div>
            </div>

            <!-- NODE 构建配置 -->
            <div v-if="formData.type === 'NODE'" style="max-width: 620px">
              <div class="form-group">
                <label class="form-label">Node 版本</label>
                <el-input v-model="buildConfig.node" placeholder="如 18.x" />
              </div>
              <div class="form-group">
                <label class="form-label">NPM 版本</label>
                <el-input v-model="buildConfig.npm" placeholder="如 9.x" />
              </div>
              <div class="form-group">
                <label class="form-label">构建命令</label>
                <el-input v-model="buildConfig.buildCommand" placeholder="如 npm run build" />
              </div>
              <div class="form-group">
                <label class="form-label">输出目录</label>
                <el-input v-model="buildConfig.outputDir" placeholder="如 dist" />
              </div>
            </div>

            <!-- PYTHON 构建配置 -->
            <div v-if="formData.type === 'PYTHON'" style="max-width: 620px">
              <div class="form-group">
                <label class="form-label">Python 版本</label>
                <el-input v-model="buildConfig.python" placeholder="如 3.10" />
              </div>
              <div class="form-group">
                <label class="form-label">依赖文件</label>
                <el-input v-model="buildConfig.requirements" placeholder="如 requirements.txt" />
              </div>
              <div class="form-group">
                <label class="form-label">运行命令</label>
                <el-input v-model="buildConfig.runCommand" placeholder="如 python main.py" />
              </div>
            </div>
          </div>

          <!-- Step 4: 完成确认 -->
          <div v-show="currentStep === 4">
            <div class="card-header">
              <div class="card-title">
                <el-icon style="color: var(--success-color)"><CircleCheck /></el-icon> Step 4 - 完成确认
              </div>
            </div>
            <div style="display: flex; flex-direction: column; gap: 16px; max-width: 620px">
              <div style="padding: 16px; border: 1px solid #ebeef5; border-radius: 10px">
                <div style="font-size: 12px; color: #909399; margin-bottom: 6px">基础信息</div>
                <div style="font-weight: 500">{{ formData.name || '-' }} / {{ formData.code || '-' }}</div>
                <div style="font-size: 13px; color: #606266; margin-top: 4px">类型：{{ getTypeDesc(formData.type) }}</div>
                <div v-if="formData.description" style="font-size: 13px; color: #909399; margin-top: 4px">
                  {{ formData.description }}
                </div>
              </div>
              <div style="padding: 16px; border: 1px solid #ebeef5; border-radius: 10px">
                <div style="font-size: 12px; color: #909399; margin-bottom: 6px">Git 与凭证</div>
                <div style="font-weight: 500; word-break: break-all">{{ formData.gitUrl || '-' }}</div>
                <div style="font-size: 13px; color: #606266; margin-top: 4px">
                  默认分支：{{ formData.defaultBranch || 'master' }}
                </div>
                <div style="font-size: 13px; color: #606266; margin-top: 4px">
                  凭证：{{ getCredentialName(formData.credentialId) || '未关联' }}
                </div>
              </div>
              <div style="padding: 16px; background: #fdf6ec; border-radius: 10px">
                <div style="font-size: 12px; color: #909399; margin-bottom: 6px">构建配置</div>
                <div style="font-size: 13px; color: #606266; line-height: 1.8">
                  {{ getBuildConfigSummary() }}
                </div>
              </div>
              <div style="padding: 14px 16px; background: #f5f7fa; border-radius: 10px">
                <div style="font-size: 12px; color: #909399; margin-bottom: 6px">创建后可继续配置</div>
                <div style="font-size: 13px; color: #606266; line-height: 1.8">
                  项目成员、环境变量、分支策略不强制在创建页完成，保存后可在项目详情页按 Tab 继续维护。
                </div>
              </div>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div style="margin-top: 24px; display: flex; gap: 12px; justify-content: flex-end">
            <el-button v-if="currentStep > 1" size="large" @click="currentStep--">上一步</el-button>
            <el-button v-if="currentStep < 4" type="primary" size="large" @click="currentStep++">下一步</el-button>
            <el-button v-if="currentStep === 4 && !isEdit" type="primary" size="large" @click="handleSubmit" :loading="submitLoading">
              创建项目
            </el-button>
            <el-button v-if="currentStep === 4 && isEdit" type="primary" size="large" @click="handleSubmit" :loading="submitLoading">
              保存修改
            </el-button>
          </div>
        </div>

        <!-- 右侧：项目类型预览卡片 -->
        <div class="card">
          <div class="card-header">
            <div class="card-title">
              <el-icon style="color: var(--primary-color)"><InfoFilled /></el-icon> {{ getTypeDesc(formData.type) }} 项目
            </div>
          </div>
          <div style="padding: 16px; background: #f8faff; border-radius: 12px; margin-bottom: 16px">
            <div style="font-weight: 500; margin-bottom: 8px">{{ getTypeDesc(formData.type) }} 项目特点</div>
            <ul style="font-size: 13px; color: #606266; line-height: 2; padding-left: 20px">
              <li v-if="formData.type === 'JAVA_MAVEN'">使用 Maven 进行依赖管理与构建</li>
              <li v-if="formData.type === 'JAVA_MAVEN'">支持 JDK 版本自定义配置</li>
              <li v-if="formData.type === 'JAVA_MAVEN'">可配置 Maven 构建命令与目标</li>
              <li v-if="formData.type === 'NODE'">使用 NPM/Yarn 管理前端依赖</li>
              <li v-if="formData.type === 'NODE'">支持自定义构建脚本</li>
              <li v-if="formData.type === 'NODE'">可配置输出目录</li>
              <li v-if="formData.type === 'PYTHON'">使用 requirements.txt 管理依赖</li>
              <li v-if="formData.type === 'PYTHON'">支持自定义运行命令</li>
              <li v-if="formData.type === 'PYTHON'">可配置 Python 版本</li>
            </ul>
          </div>
          <div style="padding: 14px; background: #fdf6ec; border-radius: 10px">
            <div style="font-weight: 500; margin-bottom: 8px">提示</div>
            <div style="font-size: 13px; color: #606266; line-height: 1.8">
              构建配置将保存为 JSON 格式，可在项目创建后随时修改。凭证信息仅存储关联关系，不会保存敏感内容。
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Document } from '@element-plus/icons-vue'
import {
  getProjectPage,
  createProject,
  updateProject,
  deleteProject,
  fetchGitBranches,
  type Project,
  type ProjectQuery,
  type CreateProjectParams
} from '@/api/project'
import { getCredentialPage, type Credential } from '@/api/credential'

// 表格数据
const loading = ref(false)
const tableData = ref<Project[]>([])
const total = ref(0)
const credentialCount = ref(0)
const credentialList = ref<Credential[]>([])
const branchList = ref<string[]>([])
const fetchingBranches = ref(false)

// 查询参数
const queryParams = reactive<ProjectQuery & { pageSize: number }>({
  page: 1,
  pageSize: 10,
  keyword: '',
  type: undefined,
  status: undefined
})

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number>()
const currentStep = ref(1)
const submitLoading = ref(false)

// 构建配置（临时对象）
const buildConfig = reactive<Record<string, string>>({})

// 表单数据
const formData = reactive<CreateProjectParams & { credentialId: number | null }>({
  name: '',
  code: '',
  type: 'JAVA_MAVEN',
  description: '',
  gitUrl: '',
  credentialId: null,
  defaultBranch: 'master',
  buildConfig: '',
  status: 1
})

const dialogTitle = computed(() => (isEdit.value ? '编辑项目' : '创建项目'))

// 类型描述映射
function getTypeDesc(type: string) {
  const map: Record<string, string> = {
    JAVA_MAVEN: 'Java Maven',
    NODE: 'Node.js',
    PYTHON: 'Python'
  }
  return map[type] || type
}

// 获取凭证名称
function getCredentialName(credentialId: number | null) {
  if (!credentialId) return null
  const cred = credentialList.value.find((c) => c.id === credentialId)
  return cred?.name
}

// 获取构建配置摘要
function getBuildConfigSummary() {
  if (!buildConfig || Object.keys(buildConfig).length === 0) {
    return '未配置构建参数'
  }
  const parts: string[] = []
  if (buildConfig.jdk) parts.push(`JDK: ${buildConfig.jdk}`)
  if (buildConfig.maven) parts.push(`Maven: ${buildConfig.maven}`)
  if (buildConfig.node) parts.push(`Node: ${buildConfig.node}`)
  if (buildConfig.npm) parts.push(`NPM: ${buildConfig.npm}`)
  if (buildConfig.python) parts.push(`Python: ${buildConfig.python}`)
  if (buildConfig.goals) parts.push(`构建命令: ${buildConfig.goals}`)
  if (buildConfig.buildCommand) parts.push(`构建命令: ${buildConfig.buildCommand}`)
  if (buildConfig.runCommand) parts.push(`运行命令: ${buildConfig.runCommand}`)
  return parts.length > 0 ? parts.join(' | ') : '未配置构建参数'
}

// 拉取 Git 分支列表
async function handleFetchBranches() {
  if (!formData.gitUrl) {
    ElMessage.warning('请先输入 Git 仓库地址')
    return
  }
  fetchingBranches.value = true
  try {
    branchList.value = await fetchGitBranches(formData.gitUrl, formData.credentialId)
    if (branchList.value.length === 0) {
      ElMessage.warning('未找到任何分支')
    } else {
      ElMessage.success(`成功获取 ${branchList.value.length} 个分支`)
      // 如果当前默认分支在列表中，选中它
      if (formData.defaultBranch && branchList.value.includes(formData.defaultBranch)) {
        // already selected
      } else if (branchList.value.length === 1) {
        formData.defaultBranch = branchList.value[0]
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取分支列表失败')
  } finally {
    fetchingBranches.value = false
  }
}

// 解析 buildConfig JSON 到 buildConfig 对象
function parseBuildConfig(jsonStr: string | null | undefined) {
  if (!jsonStr) {
    Object.keys(buildConfig).forEach((k) => delete buildConfig[k as keyof typeof buildConfig])
    return
  }
  try {
    const parsed = JSON.parse(jsonStr)
    Object.keys(buildConfig).forEach((k) => delete buildConfig[k as keyof typeof buildConfig])
    Object.assign(buildConfig, parsed)
  } catch {
    // ignore
  }
}

// 将 buildConfig 对象转为 JSON
function stringifyBuildConfig() {
  const filtered: Record<string, string> = {}
  Object.keys(buildConfig).forEach((k) => {
    if (buildConfig[k as keyof typeof buildConfig]) {
      filtered[k] = buildConfig[k as keyof typeof buildConfig] as string
    }
  })
  return Object.keys(filtered).length > 0 ? JSON.stringify(filtered) : ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await getProjectPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('加载项目列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadCredentials() {
  try {
    const res = await getCredentialPage({ page: 1, pageSize: 100 })
    credentialList.value = res.records
    credentialCount.value = res.total
  } catch (error) {
    console.error('加载凭证列表失败:', error)
  }
}

function handleQuery() {
  queryParams.page = 1
  loadData()
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  loadData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadData()
}

function handleCreate() {
  isEdit.value = false
  currentId.value = undefined
  currentStep.value = 1
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Project) {
  isEdit.value = true
  currentId.value = row.id
  currentStep.value = 1
  Object.assign(formData, {
    name: row.name,
    code: row.code,
    type: row.type,
    description: row.description || '',
    gitUrl: row.gitUrl,
    credentialId: row.credentialId,
    defaultBranch: row.defaultBranch || 'master',
    status: row.status
  })
  parseBuildConfig(row.buildConfig)
  dialogVisible.value = true
}

function handleBack() {
  dialogVisible.value = false
}

async function handleDelete(row: Project) {
  try {
    await ElMessageBox.confirm(`确定要删除项目「${row.name}」吗？删除后不可恢复。`, '提示', {
      type: 'warning'
    })
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
    }
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const submitData = {
      ...formData,
      buildConfig: stringifyBuildConfig()
    }
    if (isEdit.value && currentId.value) {
      await updateProject(currentId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createProject(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存项目失败:', error)
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  formData.name = ''
  formData.code = ''
  formData.type = 'JAVA_MAVEN'
  formData.description = ''
  formData.gitUrl = ''
  formData.credentialId = null
  formData.defaultBranch = 'master'
  formData.buildConfig = ''
  formData.status = 1
  Object.keys(buildConfig).forEach((k) => delete buildConfig[k as keyof typeof buildConfig])
}

// 监听 Git URL 和凭证变化，清空分支列表
watch(
  () => [formData.gitUrl, formData.credentialId],
  () => {
    branchList.value = []
  }
)

onMounted(() => {
  loadData()
  loadCredentials()
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
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
}

.stat-icon.green {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
}

.stat-icon.orange {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  color: white;
}

.stat-info h3 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-info p {
  color: #909399;
  font-size: 14px;
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
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #303133;
}

.w-full {
  width: 100%;
}

/* 步骤条样式 */
.steps {
  display: flex;
  justify-content: space-between;
  margin: 30px 0 40px 0;
  position: relative;
}

.step {
  flex: 1;
  text-align: center;
  position: relative;
  z-index: 1;
}

.step::after {
  content: '';
  position: absolute;
  top: 20px;
  left: calc(50% + 20px);
  width: calc(100% - 40px);
  height: 3px;
  background: #e4e7ed;
  z-index: 0;
}

.step:last-child::after {
  display: none;
}

.step.completed::after,
.step.active::after {
  background: #409eff;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e4e7ed;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  position: relative;
  z-index: 1;
  transition: all 0.3s;
  color: white;
}

.step.active .step-number {
  background: #409eff;
}

.step.completed .step-number {
  background: #67c23a;
}

.step-label {
  margin-top: 12px;
  font-size: 14px;
  color: #909399;
}

.step.active .step-label {
  color: #409eff;
  font-weight: 500;
}

/* 项目类型单选按钮 */
.type-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.type-radio:hover {
  border-color: #409eff;
}

.type-radio.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.type-radio input[type='radio'] {
  display: none;
}

/* 状态标签 */
.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-info {
  background: #ecf5ff;
  color: #409eff;
}

/* 服务器状态 */
.server-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
}

.status-dot.online {
  background: #67c23a;
}

.status-dot.offline {
  background: #909399;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-buttons .el-button.is-circle {
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>