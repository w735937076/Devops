<!--
  DRP Platform - 顶部导航组件

  @author Nick
-->
<template>
  <div class="app-header">
    <!-- 左侧：页面标题 -->
    <div class="header-left">
      <h2 class="page-title">{{ currentTitle }}</h2>
    </div>

    <!-- 右侧：用户信息 & 操作 -->
    <div class="header-right">
      <!-- 搜索 (预留) -->
      <el-button :icon="Search" text class="header-btn" />

      <!-- 通知 (预留) -->
      <el-badge :value="3" class="header-badge">
        <el-button :icon="Bell" text class="header-btn" />
      </el-badge>

      <!-- 全屏 (预留) -->
      <el-button :icon="FullScreen" text class="header-btn" @click="toggleFullScreen" />

      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" class="user-avatar">
            {{ userStore.realName?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userStore.realName || userStore.username }}</span>
          <el-icon class="user-arrow"><ArrowDown /></el-icon>
        </div>

        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              账号设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 顶部导航组件
 *
 * 功能：
 * - 显示当前页面标题
 * - 用户信息展示
 * - 快捷操作
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Search,
  Bell,
  FullScreen,
  User,
  Setting,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

// Store
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 当前页面标题
const currentTitle = computed(() => {
  return (route.meta.title as string) || 'DRP DevOps'
})

// 下拉菜单命令
async function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      // TODO: 跳转个人中心
      break
    case 'settings':
      // TODO: 跳转设置页
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
        router.push('/login')
      } catch {
        // 取消
      }
      break
  }
}

// 切换全屏
function toggleFullScreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  color: #606266;

  &:hover {
    color: #409eff;
  }
}

.header-badge {
  :deep(.el-badge__content) {
    transform: translate(50%, -50%) translateY(-2px);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f7fa;
  }
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.user-arrow {
  font-size: 12px;
  color: #909399;
}
</style>
