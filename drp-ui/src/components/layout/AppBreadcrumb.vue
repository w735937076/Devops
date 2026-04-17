<!--
  DRP Platform - 面包屑导航组件

  @author Nick
-->
<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
/**
 * 面包屑导航组件
 *
 * 功能：
 * - 自动根据路由生成面包屑
 * - 支持点击跳转
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'

// Route
const route = useRoute()

// 面包屑数据
interface Breadcrumb {
  path: string
  title: string
}

const breadcrumbs = computed<Breadcrumb[]>(() => {
  const matched = route.matched.filter((item) => item.meta && item.meta.title)

  return matched.map((item) => ({
    path: item.path,
    title: item.meta.title as string
  }))
})
</script>

<style lang="scss" scoped>
.el-breadcrumb {
  font-size: 14px;

  :deep(.el-breadcrumb__inner) {
    color: #606266;

    &.is-link:hover {
      color: #409eff;
    }
  }

  :deep(.el-breadcrumb__separator) {
    color: #c0c4cc;
  }
}
</style>
