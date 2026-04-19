<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">凭证管理</h2>
      <el-button type="primary" @click="handleCreate">添加凭证</el-button>
    </div>

    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="search-form-inline">
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="名称/账号/描述" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 180px">
            <el-option label="用户名密码" value="USERNAME_PASSWORD" />
            <el-option label="访问令牌" value="ACCESS_TOKEN" />
            <el-option label="SSH私钥" value="SSH_KEY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="name" label="凭证名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="typeDesc" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.typeDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="account" label="账号/标识" min-width="120" show-overflow-tooltip />
        <el-table-column prop="secretMasked" label="密钥内容" min-width="150" show-overflow-tooltip />
        <el-table-column prop="referencedProjectCount" label="关联项目" width="100" align="center">
          <template #default="{ row }">
            <el-badge :value="row.referencedProjectCount" :type="row.referencedProjectCount > 0 ? 'primary' : 'info'" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
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
          <el-select v-model="formData.type" placeholder="请选择凭证类型" class="w-full">
            <el-option label="用户名密码" value="USERNAME_PASSWORD" />
            <el-option label="访问令牌" value="ACCESS_TOKEN" />
            <el-option label="SSH私钥" value="SSH_KEY" />
          </el-select>
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
          <el-input v-model="formData.description" type="textarea" placeholder="请输入凭证描述" />
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
  type: [
    { required: true, message: '请选择凭证类型', trigger: 'change' }
  ],
  secretContent: [
    {
      required: true,
      validator: (rule: any, value: any, callback: any) => {
        if (!isEdit.value && !value) {
          callback(new Error('请输入密钥内容'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑凭证' : '添加凭证'))

const secretLabel = computed(() => {
  switch (formData.type) {
    case 'USERNAME_PASSWORD': return '密码'
    case 'ACCESS_TOKEN': return '令牌'
    case 'SSH_KEY': return '私钥'
    default: return '密钥'
  }
})

const secretPlaceholder = computed(() => {
  if (isEdit.value) {
    return '留空表示不修改原密钥'
  }
  switch (formData.type) {
    case 'USERNAME_PASSWORD': return '请输入 Git 密码'
    case 'ACCESS_TOKEN': return '请输入 Personal Access Token'
    case 'SSH_KEY': return '请粘贴完整的 SSH 私钥内容'
    default: return '请输入密钥内容'
  }
})

function getTypeTag(type: string) {
  switch (type) {
    case 'USERNAME_PASSWORD': return 'success'
    case 'ACCESS_TOKEN': return 'warning'
    case 'SSH_KEY': return 'danger'
    default: return 'info'
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCredentialPage(queryParams)
    tableData.value = res.records
    total.value = res.total
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

function handleReset() {
  queryParams.keyword = ''
  queryParams.type = undefined
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
    secretContent: '', // 编辑时不显示原密钥，留空表示不修改
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

<style scoped lang="scss">
.w-full {
  width: 100%;
}
</style>
