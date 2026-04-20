<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">凭证管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon><Key /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ total }}</h3>
          <p>凭证总数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon><Link /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ referencedCount }}</h3>
          <p>已被项目引用</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <h3>3</h3>
          <p>凭证类型</p>
        </div>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="card">
      <div class="card-header" style="align-items: flex-start">
        <div>
          <div class="card-title">
            <el-icon style="color: var(--primary-color)"><Key /></el-icon> 凭证列表
          </div>
          <div style="font-size: 13px; color: #909399; margin-top: 6px">
            统一管理 Git Token、用户名密码、SSH 私钥等敏感信息
          </div>
        </div>
        <div style="display: flex; gap: 12px; flex-wrap: wrap; justify-content: flex-end">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索凭证名称/账号..."
            style="width: 220px"
            clearable
            @keyup.enter="handleQuery"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="用户名密码" value="USERNAME_PASSWORD" />
            <el-option label="访问令牌" value="ACCESS_TOKEN" />
            <el-option label="SSH私钥" value="SSH_KEY" />
          </el-select>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon> 新建凭证
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="name" label="凭证名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="typeDesc" label="类型" width="120">
          <template #default="{ row }">
            <span class="status-tag" :class="getTypeClass(row.type)">{{ row.typeDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="account" label="账号/标识" min-width="150" show-overflow-tooltip />
        <el-table-column prop="secretMasked" label="密钥内容" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="font-family: monospace">{{ row.secretMasked }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="referencedProjectCount" label="关联项目" width="100" align="center">
          <template #default="{ row }">
            <el-badge :value="row.referencedProjectCount" :type="row.referencedProjectCount > 0 ? 'primary' : 'info'" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :disabled="row.referencedProjectCount > 0" @click="handleDelete(row)">删除</el-button>
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

    <!-- 创建/编辑凭证对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="凭证名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入凭证名称，如：公司主Git账号" />
        </el-form-item>
        <el-form-item label="凭证类型" prop="type">
          <div style="display: flex; gap: 12px; flex-wrap: wrap">
            <label class="type-radio" :class="{ active: formData.type === 'USERNAME_PASSWORD' }">
              <input type="radio" v-model="formData.type" value="USERNAME_PASSWORD" /> 用户名密码
            </label>
            <label class="type-radio" :class="{ active: formData.type === 'ACCESS_TOKEN' }">
              <input type="radio" v-model="formData.type" value="ACCESS_TOKEN" /> 访问令牌
            </label>
            <label class="type-radio" :class="{ active: formData.type === 'SSH_KEY' }">
              <input type="radio" v-model="formData.type" value="SSH_KEY" /> SSH私钥
            </label>
          </div>
        </el-form-item>
        <el-form-item label="账号/标识" prop="account">
          <el-input v-model="formData.account" placeholder="请输入账号（选填）" />
        </el-form-item>
        <el-form-item :label="secretLabel" prop="secretContent">
          <el-input
            v-model="formData.secretContent"
            :type="formData.type === 'SSH_KEY' ? 'textarea' : 'password'"
            :rows="formData.type === 'SSH_KEY' ? 6 : 1"
            show-password
            :placeholder="secretPlaceholder"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入凭证描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getCredentialPage,
  createCredential,
  updateCredential,
  deleteCredential,
  type Credential,
  type CredentialParams,
  type CredentialQuery
} from '@/api/credential'

// 表格数据
const loading = ref(false)
const tableData = ref<Credential[]>([])
const total = ref(0)
const referencedCount = ref(0)

// 查询参数
const queryParams = reactive<CredentialQuery & { pageSize: number }>({
  page: 1,
  pageSize: 10,
  keyword: '',
  type: undefined
})

// 创建/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number>()
const formRef = ref<FormInstance>()
const formData = reactive<CredentialParams>({
  name: '',
  type: 'USERNAME_PASSWORD',
  account: '',
  secretContent: '',
  description: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入凭证名称', trigger: 'blur' },
    { max: 100, message: '凭证名称最多100个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择凭证类型', trigger: 'change' }],
  secretContent: [
    {
      required: true,
      validator: (_rule: any, _value: any, callback: any) => {
        if (!isEdit.value && !formData.secretContent) {
          callback(new Error('请输入密钥内容'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑凭证' : '新建凭证'))

const secretLabel = computed(() => {
  switch (formData.type) {
    case 'USERNAME_PASSWORD':
      return '密码'
    case 'ACCESS_TOKEN':
      return '令牌'
    case 'SSH_KEY':
      return '私钥'
    default:
      return '密钥'
  }
})

const secretPlaceholder = computed(() => {
  if (isEdit.value) {
    return '留空表示不修改原密钥'
  }
  switch (formData.type) {
    case 'USERNAME_PASSWORD':
      return '请输入 Git 密码'
    case 'ACCESS_TOKEN':
      return '请输入 Personal Access Token'
    case 'SSH_KEY':
      return '请粘贴完整的 SSH 私钥内容'
    default:
      return '请输入密钥内容'
  }
})

function getTypeClass(type: string) {
  switch (type) {
    case 'USERNAME_PASSWORD':
      return 'status-success'
    case 'ACCESS_TOKEN':
      return 'status-warning'
    case 'SSH_KEY':
      return 'status-danger'
    default:
      return 'status-info'
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCredentialPage(queryParams)
    tableData.value = res.records
    total.value = res.total
    referencedCount.value = res.records.filter((c) => c.referencedProjectCount > 0).length
  } catch (error) {
    console.error('加载凭证列表失败:', error)
  } finally {
    loading.value = false
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
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Credential) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(formData, {
    name: row.name,
    type: row.type,
    account: row.account,
    secretContent: '',
    description: row.description
  })
  dialogVisible.value = true
}

async function handleDelete(row: Credential) {
  try {
    await ElMessageBox.confirm(
      `确定要删除凭证「${row.name}」吗？${row.referencedProjectCount > 0 ? '\n注意：该凭证已被项目引用，删除可能导致构建失败。' : ''}`,
      '提示',
      { type: 'warning' }
    )
    await deleteCredential(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除凭证失败:', error)
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value && currentId.value) {
          await updateCredential(currentId.value, formData)
          ElMessage.success('更新成功')
        } else {
          await createCredential(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('保存凭证失败:', error)
      }
    }
  })
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

function resetForm() {
  formData.name = ''
  formData.type = 'USERNAME_PASSWORD'
  formData.account = ''
  formData.secretContent = ''
  formData.description = ''
}

onMounted(() => {
  loadData()
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

/* 凭证类型单选按钮 */
.type-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
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

.status-success {
  background: #e1f3d8;
  color: #67c23a;
}

.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-danger {
  background: #fef0f0;
  color: #f56c6c;
}

.status-info {
  background: #ecf5ff;
  color: #409eff;
}
</style>