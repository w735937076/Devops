<!--
  DRP Platform - 表格组件封装

  功能：
  - 统一表格样式
  - 加载状态
  - 空数据提示
  - 行选择
  - 斑马纹

  @author Nick
-->
<template>
  <div class="drp-table">
    <el-table
      v-loading="loading"
      :data="data"
      :columns="columns"
      :stripe="stripe"
      :border="border"
      :size="size"
      :height="height"
      :max-height="maxHeight"
      :row-key="rowKey"
      :selection="selection"
      @selection-change="handleSelectionChange"
      @select="handleSelect"
      @select-all="handleSelectAll"
    >
      <!-- 默认插槽 -->
      <slot />

      <!-- 空数据插槽 -->
      <template #empty>
        <div class="table-empty">
          <el-icon :size="48"><Document /></el-icon>
          <p>{{ emptyText }}</p>
        </div>
      </template>
    </el-table>
  </div>
</template>

<script setup lang="ts">
/**
 * 表格组件
 *
 * Props:
 * - data: 表格数据
 * - columns: 列配置（简化配置）
 * - loading: 加载状态
 * - stripe: 斑马纹
 * - border: 边框
 * - size: 表格尺寸
 * - height: 固定高度
 * - maxHeight: 最大高度
 * - rowKey: 行数据唯一标识
 */
import { Document } from '@element-plus/icons-vue'
import type { TableColumnCtx } from 'element-plus'

interface Column {
  prop?: string
  label?: string
  width?: string | number
  minWidth?: string | number
  align?: 'left' | 'center' | 'right'
  fixed?: 'left' | 'right'
  slot?: string
  formatter?: (row: any, column: TableColumnCtx<any>, cellValue: any) => any
}

interface Props {
  data?: any[]
  columns?: Column[]
  loading?: boolean
  stripe?: boolean
  border?: boolean
  size?: 'large' | 'default' | 'small'
  height?: string | number
  maxHeight?: string | number
  rowKey?: string
  selection?: boolean
  emptyText?: string
}

withDefaults(defineProps<Props>(), {
  data: () => [],
  columns: () => [],
  loading: false,
  stripe: true,
  border: true,
  size: 'default',
  rowKey: 'id',
  selection: false,
  emptyText: '暂无数据'
})

const emit = defineEmits<{
  'selection-change': [selection: any[]]
  select: [selection: any[], row: any]
  'select-all': [selection: any[]]
}>()

function handleSelectionChange(selection: any[]) {
  emit('selection-change', selection)
}

function handleSelect(selection: any[], row: any) {
  emit('select', selection, row)
}

function handleSelectAll(selection: any[]) {
  emit('select-all', selection)
}
</script>

<style lang="scss" scoped>
.drp-table {
  width: 100%;
}

.table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #c0c4cc;

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}

:deep(.el-table) {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #fafafa;

  border-radius: 8px;
  overflow: hidden;

  th.el-table__cell {
    background-color: var(--el-table-header-bg-color);
    font-weight: 600;
    color: #303133;
  }

  td.el-table__cell {
    padding: 12px 0;
  }

  .el-table__body tr:hover > td.el-table__cell {
    background-color: #f5f7fa !important;
  }
}
</style>
