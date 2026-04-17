<!--
  DRP Platform - 日志查看器组件

  功能：
  - 日志高亮显示
  - 关键字搜索
  - 自动滚动
  - 日志下载

  @author Nick
-->
<template>
  <div class="log-viewer" :style="{ height: height }">
    <!-- 工具栏 -->
    <div class="log-toolbar">
      <el-input
        v-model="filterText"
        placeholder="搜索日志..."
        :prefix-icon="Search"
        clearable
        class="log-search"
        @input="handleFilter"
      />
      <div class="toolbar-actions">
        <el-button :icon="Refresh" circle size="small" @click="handleRefresh" />
        <el-button :icon="FullScreen" circle size="small" @click="toggleFullScreen" />
        <el-button :icon="Download" circle size="small" @click="handleDownload" />
        <el-button :icon="Delete" circle size="small" @click="handleClear" />
      </div>
    </div>

    <!-- 日志内容 -->
    <div ref="logContainer" class="log-content" @scroll="handleScroll">
      <div v-if="!filteredLogs.length" class="log-empty">
        <el-icon :size="48"><Document /></el-icon>
        <p>暂无日志</p>
      </div>
      <div
        v-for="(line, index) in filteredLogs"
        :key="index"
        :class="['log-line', getLineClass(line)]"
      >
        <span class="line-number">{{ index + 1 }}</span>
        <span class="line-content" v-html="highlightLine(line)" />
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="log-statusbar">
      <span class="status-item">
        <el-icon><Filter /></el-icon>
        {{ filteredLogs.length }} / {{ logs.length }} 行
      </span>
      <span class="status-item">
        <el-icon><Top /></el-icon>
        {{ autoScroll ? '自动滚动' : '已暂停' }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 日志查看器组件
 *
 * Props:
 * - logs: 日志数组
 * - height: 组件高度
 * - autoScroll: 是否自动滚动
 *
 * Events:
 * - refresh: 刷新日志
 */
import { ref, computed, watch, nextTick } from 'vue'
import {
  Search,
  Refresh,
  FullScreen,
  Download,
  Delete,
  Document,
  Filter,
  Top
} from '@element-plus/icons-vue'
import ansidec from 'ansidec'

// =====================================================
// Props & Emits
// =====================================================

interface Props {
  logs?: string[]
  height?: string
  autoScroll?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  logs: () => [],
  height: '400px',
  autoScroll: true
})

const emit = defineEmits<{
  refresh: []
}>()

// =====================================================
// Refs
// =====================================================

const logContainer = ref<HTMLElement>()
const filterText = ref('')

// =====================================================
// Computed
// =====================================================

/**
 * 过滤后的日志
 */
const filteredLogs = computed(() => {
  if (!filterText.value) {
    return props.logs
  }
  const keyword = filterText.value.toLowerCase()
  return props.logs.filter((log) => log.toLowerCase().includes(keyword))
})

// =====================================================
// Methods
// =====================================================

/**
 * 获取行样式类
 */
function getLineClass(line: string): string {
  const lowerLine = line.toLowerCase()
  if (lowerLine.includes('error') || lowerLine.includes('exception')) {
    return 'log-error'
  }
  if (lowerLine.includes('warn')) {
    return 'log-warn'
  }
  if (lowerLine.includes('info')) {
    return 'log-info'
  }
  if (lowerLine.includes('debug')) {
    return 'log-debug'
  }
  return ''
}

/**
 * 高亮显示行内容
 */
function highlightLine(line: string): string {
  if (!line) return ''

  // 使用 ansidec 解析 ANSI 颜色
  let highlighted = ansidec(line)

  // 如果搜索关键字存在，高亮关键字
  if (filterText.value) {
    const keyword = filterText.value
    const regex = new RegExp(`(${escapeRegex(keyword)})`, 'gi')
    highlighted = highlighted.replace(regex, '<mark class="highlight">$1</mark>')
  }

  return highlighted.replace(/\n/g, '<br>')
}

/**
 * 转义正则特殊字符
 */
function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 处理滚动
 */
function handleScroll() {
  // 用户手动滚动时暂停自动滚动
  // 可扩展实现
}

/**
 * 过滤日志
 */
function handleFilter() {
  nextTick(scrollToBottom)
}

/**
 * 刷新日志
 */
function handleRefresh() {
  emit('refresh')
}

/**
 * 清空日志
 */
function handleClear() {
  // 清空操作应由父组件处理
}

/**
 * 下载日志
 */
function handleDownload() {
  const content = filteredLogs.value.join('\n')
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `log-${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

/**
 * 切换全屏
 */
function toggleFullScreen() {
  if (!document.fullscreenElement) {
    logContainer.value?.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

/**
 * 滚动到底部
 */
function scrollToBottom() {
  if (props.autoScroll && logContainer.value) {
    logContainer.value.scrollTop = logContainer.value.scrollHeight
  }
}

// =====================================================
// Watch
// =====================================================

watch(
  () => props.logs.length,
  () => {
    nextTick(scrollToBottom)
  }
)
</script>

<style lang="scss" scoped>
.log-viewer {
  display: flex;
  flex-direction: column;
  border: 1px solid #3e3e3e;
  border-radius: 8px;
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  overflow: hidden;
}

// 工具栏
.log-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #2d2d2d;
  border-bottom: 1px solid #3e3e3e;
}

.log-search {
  flex: 1;
  max-width: 300px;

  :deep(.el-input__wrapper) {
    background: #1e1e1e;
    border: 1px solid #3e3e3e;
    box-shadow: none;

    &:hover,
    &.is-focus {
      border-color: #667eea;
    }
  }

  :deep(.el-input__inner) {
    color: #d4d4d4;

    &::placeholder {
      color: #6e6e6e;
    }
  }
}

.toolbar-actions {
  display: flex;
  gap: 4px;

  .el-button {
    background: #3e3e3e;
    border: none;
    color: #d4d4d4;

    &:hover {
      background: #4e4e4e;
      color: #667eea;
    }
  }
}

// 日志内容区
.log-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  line-height: 1.6;

  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }

  &::-webkit-scrollbar-thumb {
    background: #4e4e4e;
    border-radius: 4px;
  }

  &::-webkit-scrollbar-track {
    background: #2d2d2d;
  }
}

.log-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6e6e6e;

  p {
    margin-top: 12px;
  }
}

.log-line {
  display: flex;
  white-space: pre-wrap;
  word-break: break-all;
  min-height: 1.6em;

  &.log-error {
    background: rgba(245, 108, 108, 0.15);
    color: #f48771;
  }

  &.log-warn {
    background: rgba(204, 167, 0, 0.15);
    color: #cca700;
  }

  &.log-info {
    color: #4ec9b0;
  }

  &.log-debug {
    color: #9cdcfe;
  }
}

.line-number {
  flex-shrink: 0;
  width: 50px;
  padding-right: 12px;
  text-align: right;
  color: #6e6e6e;
  user-select: none;
}

.line-content {
  flex: 1;

  :deep(.highlight) {
    background: #6536a3;
    color: #fff;
    padding: 0 2px;
    border-radius: 2px;
  }
}

// 状态栏
.log-statusbar {
  display: flex;
  gap: 16px;
  padding: 8px 12px;
  background: #2d2d2d;
  border-top: 1px solid #3e3e3e;
  font-size: 12px;
  color: #6e6e6e;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
