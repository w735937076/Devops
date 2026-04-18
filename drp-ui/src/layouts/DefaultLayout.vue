<!--
  DRP Platform - 默认布局组件

  包含：侧边栏、顶部导航、面包屑、主内容区

  @author Nick
-->
<template>
  <el-container class="default-layout">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
      <AppSidebar />
    </el-aside>

    <!-- 主体区域 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header height="60px">
        <AppHeader />
      </el-header>

      <!-- 面包屑 -->
      <div class="breadcrumb-container">
        <AppBreadcrumb />
      </div>

      <!-- 主内容 -->
      <el-main>
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
/**
 * 默认布局组件
 *
 * 功能：
 * - 提供完整的页面布局结构
 * - 响应式侧边栏折叠
 */
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppBreadcrumb from '@/components/layout/AppBreadcrumb.vue'

// Store
const appStore = useAppStore()

// 侧边栏宽度（响应式）
const sidebarWidth = computed(() => appStore.sidebarWidth)
</script>

<style lang="scss" scoped>
.default-layout {
  height: 100vh;
  overflow: hidden;
}

.el-aside {
  transition: width 0.3s ease;
  overflow-x: hidden;
  overflow-y: auto;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);

  // 滚动条样式
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }

  &.is-collapsed {
    width: 64px !important;
  }
}

.el-header {
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.breadcrumb-container {
  padding: 12px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.el-main {
  padding: 20px;
  overflow: hidden;
  background: #f5f7fa;

  // 滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.25);
    }
  }
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
