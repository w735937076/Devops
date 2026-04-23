<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">构建记录</h2>
      <div class="page-actions">
        <el-button type="primary" @click="showTriggerDialog = true">
          <el-icon><Plus /></el-icon> 触发构建
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="queryParams.projectId" placeholder="选择项目" clearable @change="loadBuildList">
        <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="构建状态" clearable @change="loadBuildList">
        <el-option v-for="(item, key) in BuildStatusMap" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-input v-model="queryParams.branch" placeholder="分支名称" clearable @change="loadBuildList" />
      <el-button @click="loadBuildList">查询</el-button>
    </div>

    <!-- 构建列表 -->
    <div class="card-container">
      <el-table :data="buildList" v-loading="loading" stripe>
        <el-table-column prop="buildNumber" label="构建号" width="100">
          <template #default="{ row }">
            <span class="build-number">#{{ row.buildNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="项目" min-width="120" />
        <el-table-column prop="branch" label="分支" min-width="120">
          <template #default="{ row }">
            <el-tag size="small">
              <el-icon><Folder /></el-icon> {{ row.branch }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="commitId" label="Commit ID" min-width="140">
          <template #default="{ row }">
            <code class="commit-id">{{ row.commitId?.substring(0, 7) }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="BuildStatusMap[row.status]?.type">
              {{ BuildStatusMap[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="triggerUser" label="触发人" width="100" />
        <el-table-column prop="createTime" label="构建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="查看详情" placement="top">
                <el-button type="primary" circle @click="goToDetail(row.id)">
                  <el-icon><Document /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="row.artifacts?.length" content="下载产物" placement="top">
                <el-dropdown trigger="click" @command="(cmd: any) => handleDownloadArtifact(cmd)">
                  <el-button type="success" circle>
                    <el-icon><Download /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="(artifact, idx) in row.artifacts"
                        :key="idx"
                        :command="artifact"
                      >
                        <el-icon><Folder /></el-icon>
                        {{ artifact.name }}
                        <span style="color: #909399; margin-left: 8px; font-size: 12px;">{{ formatSize(artifact.size) }}</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-tooltip>
              <el-tooltip v-if="row.status === 0 || row.status === 1" content="取消构建" placement="top">
                <el-button type="danger" circle @click="handleCancel(row.id)">
                  <el-icon><Close /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 2" content="重新构建" placement="top">
                <el-button type="warning" circle @click="handleRetry(row.id)">
                  <el-icon><RefreshRight /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadBuildList"
        />
      </div>
    </div>

    <!-- 触发构建对话框 -->
    <el-dialog v-model="showTriggerDialog" title="触发构建" width="500px">
      <el-form :model="triggerForm" label-width="100px">
        <el-form-item label="项目" required>
          <el-select v-model="triggerForm.projectId" placeholder="选择项目" @change="onProjectChange">
            <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分支" required>
          <el-input v-model="triggerForm.branch" placeholder="分支名称，如 master" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTriggerDialog = false">取消</el-button>
        <el-button type="primary" @click="handleTrigger">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Folder, Download, Document, Close, RefreshRight } from '@element-plus/icons-vue'
import axios from 'axios'
import { getBuildList, triggerBuild, cancelBuild, BuildStatusMap, BuildStatus } from '@/api/build'
import { getProjectPage } from '@/api/project'
import type { Build, BuildQuery, Artifact } from '@/api/build'

const router = useRouter()

const loading = ref(false)
const buildList = ref<Build[]>([])
const total = ref(0)
const projectList = ref<any[]>([])
const showTriggerDialog = ref(false)

const queryParams = reactive<BuildQuery>({
  projectId: undefined,
  status: undefined,
  branch: undefined,
  page: 1,
  pageSize: 10
})

const triggerForm = reactive({
  projectId: undefined as number | undefined,
  branch: 'master'
})

onMounted(() => {
  loadProjectList()
  loadBuildList()
})

async function loadProjectList() {
  try {
    const data = await getProjectPage({ page: 1, pageSize: 100 })
    projectList.value = data.records
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

async function loadBuildList() {
  loading.value = true
  try {
    const data = await getBuildList(queryParams)
    buildList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载构建列表失败')
  } finally {
    loading.value = false
  }
}

function onProjectChange(projectId: number) {
  const project = projectList.value.find(p => p.id === projectId)
  if (project) {
    triggerForm.branch = project.defaultBranch || 'master'
  }
}

async function handleTrigger() {
  if (!triggerForm.projectId || !triggerForm.branch) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    await triggerBuild({
      projectId: triggerForm.projectId,
      branch: triggerForm.branch
    })
    ElMessage.success('构建已触发')
    showTriggerDialog.value = false
    loadBuildList()
  } catch (error) {
    ElMessage.error('触发构建失败')
  }
}

async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定要取消该构建吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelBuild(id)
    ElMessage.success('构建已取消')
    loadBuildList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消构建失败')
    }
  }
}

function goToDetail(id: number) {
  router.push(`/builds/${id}`)
}

function handleRetry(id: number) {
  // TODO: 实现重新构建功能
  ElMessage.info('重新构建功能开发中')
}

function formatDuration(seconds: number): string {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}分${secs}秒` : `${secs}秒`
}

function formatSize(bytes: number): string {
  if (!bytes) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024
    i++
  }
  return `${bytes.toFixed(1)} ${units[i]}`
}

async function handleDownloadArtifact(artifact: Artifact) {
  if (!artifact.downloadUrl) {
    ElMessage.warning('下载链接不存在')
    return
  }
  try {
    const token = localStorage.getItem('accessToken')
    const response = await axios.get(artifact.downloadUrl, {
      responseType: 'blob',
      headers: { Authorization: `Bearer ${token}` }
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = artifact.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
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

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.build-number {
  font-weight: 600;
  color: var(--el-color-primary);
}

.commit-id {
  font-family: 'Consolas', monospace;
  color: #666;
  font-size: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
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
