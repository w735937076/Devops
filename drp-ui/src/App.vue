<!--
  DRP Platform - 根组件

  @author Nick
-->
<template>
  <router-view />
</template>

<script setup lang="ts">
/**
 * 应用根组件
 */
import { useAppStore } from '@/stores/app'
import { onMounted, onUnmounted } from 'vue'

// Store
const appStore = useAppStore()

// 初始化
onMounted(() => {
  // 恢复侧边栏状态
  appStore.initSidebarState()

  // 监听窗口尺寸变化
  window.addEventListener('resize', appStore.handleResize)
})

// 清理
onUnmounted(() => {
  window.removeEventListener('resize', appStore.handleResize)
})
</script>

<style lang="scss">
@import '@/assets/styles/global.scss';
</style>
