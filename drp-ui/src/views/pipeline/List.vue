<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">流水线配置</h2>
      <div class="page-actions">
        <el-select v-model="selectedProjectId" placeholder="选择项目" clearable @change="loadPipelineList" style="width: 200px; margin-right: 12px;">
          <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button type="primary" @click="showCreateDialog = true" :disabled="!selectedProjectId">
          <el-icon><Plus /></el-icon> 新建流水线
        </el-button>
      </div>
    </div>

    <!-- 流水线列表 -->
    <div class="card-container">
      <el-table :data="pipelineList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="流水线名称" min-width="150">
          <template #default="{ row }">
            <span class="pipeline-name">{{ row.name }}</span>
            <el-tag v-if="row.isDefault" size="small" type="primary" style="margin-left: 8px;">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="阶段数" width="100">
          <template #default="{ row }">
            {{ getStagesCount(row.stages) }} 个阶段
          </template>
        </el-table-column>
        <el-table-column label="缓存" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.buildParams" size="small" type="success">启用</el-tag>
            <el-tag v-else size="small" type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeout" label="超时时间" width="120">
          <template #default="{ row }">
            {{ row.timeout }}秒
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="编辑" placement="top">
                <el-button type="primary" circle @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button type="danger" circle @click="handleDelete(row.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑流水线对话框 -->
    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑流水线' : '新建流水线'" width="700px">
      <el-form :model="pipelineForm" label-width="100px">
        <el-form-item label="流水线名称" required>
          <el-input v-model="pipelineForm.name" placeholder="请输入流水线名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="pipelineForm.description" type="textarea" rows="2" placeholder="可选，描述流水线的用途" />
        </el-form-item>
        <el-form-item label="超时时间">
          <el-input-number v-model="pipelineForm.timeout" :min="60" :max="7200" :step="60" /> 秒
        </el-form-item>
        <el-form-item label="阶段配置">
          <div class="stages-editor">
            <div v-for="(stage, index) in pipelineForm.stages" :key="index" class="stage-item">
              <el-select v-model="stage.type" placeholder="选择阶段类型" style="width: 150px;">
                <el-option label="代码Checkout" value="GIT_CLONE" />
                <el-option label="Maven构建" value="MAVEN_BUILD" />
                <el-option label="NPM构建" value="NPM_BUILD" />
                <el-option label="Python构建" value="PYTHON_BUILD" />
              </el-select>
              <el-input v-model="stage.name" placeholder="阶段名称" style="width: 120px;" />
              <el-checkbox v-model="stage.enabled">启用</el-checkbox>
              <el-button type="danger" link @click="removeStage(index)">删除</el-button>
            </div>
            <el-button type="primary" link @click="addStage">
              <el-icon><Plus /></el-icon> 添加阶段
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getPipelineList, createPipeline, updatePipeline, deletePipeline } from '@/api/build'
import { getProjectPage } from '@/api/project'
import type { Pipeline, StageConfig } from '@/api/build'

const loading = ref(false)
const projectList = ref<any[]>([])
const selectedProjectId = ref<number | undefined>(undefined)
const pipelineList = ref<Pipeline[]>([])
const showCreateDialog = ref(false)
const isEdit = ref(false)
const editingId = ref<number | undefined>(undefined)

const pipelineForm = reactive<{
  name: string
  description: string
  timeout: number
  stages: StageConfig[]
}>({
  name: '',
  description: '',
  timeout: 3600,
  stages: [
    { name: 'checkout', type: 'GIT_CLONE', enabled: true },
    { name: 'build', type: 'MAVEN_BUILD', enabled: true }
  ]
})

onMounted(() => {
  loadProjectList()
})

async function loadProjectList() {
  try {
    const data = await getProjectPage({ page: 1, pageSize: 100 })
    projectList.value = data.records
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

async function loadPipelineList() {
  if (!selectedProjectId.value) {
    pipelineList.value = []
    return
  }

  loading.value = true
  try {
    pipelineList.value = await getPipelineList(selectedProjectId.value)
  } catch (error) {
    ElMessage.error('加载流水线列表失败')
  } finally {
    loading.value = false
  }
}

function handleEdit(row: Pipeline) {
  isEdit.value = true
  editingId.value = row.id
  pipelineForm.name = row.name
  pipelineForm.description = row.description || ''
  pipelineForm.timeout = row.timeout || 3600
  // 解析 stages JSON 字符串为数组
  try {
    pipelineForm.stages = typeof row.stages === 'string' ? JSON.parse(row.stages) : row.stages || []
  } catch {
    pipelineForm.stages = []
  }
  showCreateDialog.value = true
}

function getStagesCount(stages: any): number {
  if (!stages) return 0
  if (Array.isArray(stages)) return stages.length
  try {
    const parsed = typeof stages === 'string' ? JSON.parse(stages) : stages
    return Array.isArray(parsed) ? parsed.length : 0
  } catch {
    return 0
  }
}

async function handleSave() {
  if (!pipelineForm.name) {
    ElMessage.warning('请输入流水线名称')
    return
  }

  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择项目')
    return
  }

  try {
    const data = {
      name: pipelineForm.name,
      description: pipelineForm.description,
      timeout: pipelineForm.timeout,
      stages: JSON.stringify(pipelineForm.stages),
      status: 1
    }

    if (isEdit.value && editingId.value) {
      await updatePipeline(selectedProjectId.value, editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createPipeline(selectedProjectId.value, data)
      ElMessage.success('创建成功')
    }

    showCreateDialog.value = false
    loadPipelineList()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该流水线吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePipeline(selectedProjectId.value!, id)
    ElMessage.success('删除成功')
    loadPipelineList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function addStage() {
  pipelineForm.stages.push({
    name: '',
    type: 'MAVEN_BUILD',
    enabled: true
  })
}

function removeStage(index: number) {
  pipelineForm.stages.splice(index, 1)
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.pipeline-name {
  font-weight: 500;
}

.stages-editor {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #f5f7fa;
}

.stage-item {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.stage-item:last-child {
  margin-bottom: 0;
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
