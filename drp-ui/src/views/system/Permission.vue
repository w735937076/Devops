<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">权限管理</h2>
      <el-button type="primary" @click="handleCreate">添加权限</el-button>
    </div>

    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="search-form-inline">
        <el-form-item label="权限编码">
          <el-input v-model="queryParams.code" placeholder="请输入权限编码" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="权限名称">
          <el-input v-model="queryParams.name" placeholder="请输入权限名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
            <el-option label="API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <el-table v-loading="loading" :data="tableData" stripe default-expand-all row-key="id" class="permission-table">
        <el-table-column prop="code" label="权限编码" min-width="150" />
        <el-table-column prop="name" label="权限名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'MENU'" type="success">菜单</el-tag>
            <el-tag v-else-if="row.type === 'BUTTON'" type="warning">按钮</el-tag>
            <el-tag v-else type="info">API</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
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

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 创建/编辑权限对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="formData.code" :disabled="isEdit" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择权限类型" style="width: 100%">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
            <el-option label="API接口" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="父级权限">
          <el-select v-model="formData.parentId" placeholder="请选择父级权限" clearable style="width: 100%">
            <el-option v-for="perm in flatPermissions" :key="perm.id" :label="perm.name" :value="perm.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="formData.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="formData.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { Edit, Delete } from '@element-plus/icons-vue'
import {
  queryPermissions,
  createPermission,
  updatePermission,
  deletePermission,
  getAllPermissions,
  type Permission,
  type CreatePermissionRequest,
  type UpdatePermissionRequest
} from '@/api/permission'

interface PermissionFormData extends Partial<CreatePermissionRequest> {
  id?: number
  code: string
  name: string
  type: string
  description: string
  parentId?: number
  sort: number
  icon: string
  path: string
  component: string
  status: number
}

// 表格数据
const loading = ref(false)
const tableData = ref<Permission[]>([])
const flatPermissions = ref<Permission[]>([])
const total = ref(0)

// 分页
function handleSizeChange(size: number) {
  queryParams.size = size
  loadData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadData()
}

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 100,
  code: '',
  name: '',
  type: '',
  status: undefined as number | undefined
})

// 创建/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive<PermissionFormData>({
  code: '',
  name: '',
  description: '',
  parentId: undefined,
  type: 'API',
  sort: 0,
  icon: '',
  path: '',
  component: '',
  status: 1
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ]
}

// 计算对话框标题
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑权限' : '添加权限'
})

// 加载表格数据
async function loadData() {
  loading.value = true
  try {
    const res = await queryPermissions(queryParams)
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('加载权限列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载扁平权限列表（用于父级选择）
async function loadFlatPermissions() {
  try {
    flatPermissions.value = await getAllPermissions()
  } catch (error) {
    console.error('加载权限列表失败:', error)
  }
}

// 查询
function handleQuery() {
  queryParams.page = 1
  loadData()
}

// 重置
function handleReset() {
  queryParams.code = ''
  queryParams.name = ''
  queryParams.type = ''
  queryParams.status = undefined
  queryParams.page = 1
  loadData()
}

// 创建
function handleCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: Permission) {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    code: row.code,
    name: row.name,
    description: row.description || '',
    parentId: row.parentId || undefined,
    type: row.type,
    sort: row.sort || 0,
    icon: row.icon || '',
    path: row.path || '',
    component: row.component || '',
    status: row.status
  })
  dialogVisible.value = true
}

// 删除
async function handleDelete(row: Permission) {
  try {
    await ElMessageBox.confirm(`确定要删除权限「${row.name}」吗？`, '提示', {
      type: 'warning'
    })
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
    }
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updatePermission(formData as UpdatePermissionRequest)
          ElMessage.success('更新成功')
        } else {
          await createPermission(formData as CreatePermissionRequest)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('保存权限失败:', error)
      }
    }
  })
}

// 重置表单
function resetForm() {
  formData.code = ''
  formData.name = ''
  formData.description = ''
  formData.parentId = undefined
  formData.type = 'API'
  formData.sort = 0
  formData.icon = ''
  formData.path = ''
  formData.component = ''
  formData.status = 1
}

// 对话框关闭
function handleDialogClose() {
  formRef.value?.resetFields()
  resetForm()
}

// 初始化
onMounted(() => {
  loadData()
  loadFlatPermissions()
})
</script>

<style scoped>
.page-container {
  height: calc(100vh - 60px - 44px - 40px);
  padding: 0;
  display: flex;
  flex-direction: column;
}

.page-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.search-form {
  flex-shrink: 0;
  background: #fff;
  padding: 16px 20px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.search-form-inline {
  margin-bottom: 0;
}

.card-container {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.permission-table {
  flex: 1;
  overflow-y: auto;
}

:deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

:deep(.el-table__body) {
  display: table-row-group;
}

.pagination-container {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
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
</style>
