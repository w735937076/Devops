<!--
  DRP Platform - 分页组件

  @author Nick
-->
<template>
  <div class="drp-page">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :background="background"
      :small="small"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

/**
 * 分页组件
 *
 * Props:
 * - total: 总记录数
 * - page: 当前页码
 * - limit: 每页条数
 * - pageSizes: 每页条数选项
 */
interface Props {
  total?: number
  page?: number
  limit?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
  small?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  total: 0,
  page: 1,
  limit: 10,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  small: false
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:limit': [limit: number]
  'change': [page: number, limit: number]
}>()

// Refs
const currentPage = ref(props.page)
const pageSize = ref(props.limit)

// 监听变化
watch(
  () => props.page,
  (val) => {
    currentPage.value = val
  }
)

watch(
  () => props.limit,
  (val) => {
    pageSize.value = val
  }
)

/**
 * 每页条数变化
 */
function handleSizeChange(size: number) {
  emit('update:limit', size)
  emit('change', currentPage.value, size)
}

/**
 * 页码变化
 */
function handleCurrentChange(page: number) {
  emit('update:page', page)
  emit('change', page, pageSize.value)
}
</script>

<style lang="scss" scoped>
.drp-page {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
