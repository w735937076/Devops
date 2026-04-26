<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
      <el-button type="primary" @click="handleCreate">添加角色</el-button>
    </div>

    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="search-form-inline">
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.code" placeholder="请输入角色编码" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.name" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" />
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
      <el-table v-loading="loading" :data="tableData" stripe class="role-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="角色编码" min-width="120" />
        <el-table-column prop="name" label="角色名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'SYSTEM'" type="warning">系统</el-tag>
            <el-tag v-else type="info">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="编辑" placement="top">
                <el-button type="primary" circle @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="分配权限" placement="top">
                <el-button type="warning" circle @click="handleAssignPermissions(row)">
                  <el-icon><Key /></el-icon>
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

    <!-- 创建/编辑角色对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="formData.code" :disabled="isEdit" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
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

    <!-- 分配权限对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="600px">
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        :check-strictly="false"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import { Edit, Delete, Key } from '@element-plus/icons-vue'
import { queryRoles, createRole, updateRole, deleteRole, assignRolePermissions, type Role, type CreateRoleRequest, type UpdateRoleRequest } from '@/api/role'
import { getPermissionTree, getPermissionIdsByRoleId, type Permission } from '@/api/permission'

interface RoleFormData extends Partial<CreateRoleRequest> {
  id?: number
  code: string
  name: string
  description: string
  sort: number
  status: number
}

// 表格数据
const loading = ref(false)
const tableData = ref<Role[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  code: '',
  name: '',
  status: undefined as number | undefined
})

// 创建/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive<RoleFormData>({
  code: '',
  name: '',
  description: '',
  sort: 0,
  status: 1
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 50, message: '角色编码长度为2-50个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ]
}

// 分配权限对话框
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref<InstanceType<typeof ElTree>>()
const permissionTree = ref<Permission[]>([])
const currentRoleId = ref<number>()
const currentRoleName = ref('')

// 计算对话框标题
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑角色' : '添加角色'
})

// 加载表格数据
async function loadData() {
  loading.value = true
  try {
    const res = await queryRoles(queryParams)
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
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
  queryParams.status = undefined
  queryParams.page = 1
  loadData()
}

// 分页
function handleSizeChange(size: number) {
  queryParams.size = size
  loadData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  loadData()
}

// 创建
function handleCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: Role) {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    code: row.code,
    name: row.name,
    description: row.description || '',
    sort: row.sort || 0,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除
async function handleDelete(row: Role) {
  try {
    await ElMessageBox.confirm(`确定要删除角色「${row.name}」吗？`, '提示', {
      type: 'warning'
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
    }
  }
}

// 分配权限
async function handleAssignPermissions(row: Role) {
  currentRoleId.value = row.id
  currentRoleName.value = row.name

  try {
    // 加载权限树
    permissionTree.value = await getPermissionTree()

    // 加载角色当前权限
    const currentPermissionIds = await getPermissionIdsByRoleId(row.id)

    // 设置选中状态
    permissionDialogVisible.value = true

    // 等待对话框渲染完成后设置选中
    setTimeout(() => {
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(currentPermissionIds)
      }
    }, 100)
  } catch (error) {
    console.error('加载权限数据失败:', error)
    ElMessage.error('加载权限数据失败')
  }
}

// 提交权限分配
async function handleSubmitPermissions() {
  if (!currentRoleId.value || !permissionTreeRef.value) return

  try {
    // 获取所有选中节点（包括半选中状态）
    const checkedKeys = permissionTreeRef.value.getCheckedKeys(true)
    await assignRolePermissions(currentRoleId.value, checkedKeys as number[])
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('分配权限失败:', error)
    ElMessage.error('分配权限失败')
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateRole(formData as UpdateRoleRequest)
          ElMessage.success('更新成功')
        } else {
          await createRole(formData as CreateRoleRequest)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('保存角色失败:', error)
      }
    }
  })
}

// 重置表单
function resetForm() {
  formData.code = ''
  formData.name = ''
  formData.description = ''
  formData.sort = 0
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

.role-table {
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
