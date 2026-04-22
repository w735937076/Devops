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

    <!-- 右侧：操作区域 -->
    <div class="header-right">
      <!-- 全局搜索框 -->
      <div class="search-wrapper">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索..."
          class="header-search"
          clearable
        >
          <template #prefix>
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
            </svg>
          </template>
        </el-input>
      </div>

      <!-- 通知铃铛 -->
      <div class="action-item" @click="handleNotification">
        <div class="icon-wrapper">
          <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          <span v-if="notificationCount > 0" class="badge">{{ notificationCount > 99 ? '99+' : notificationCount }}</span>
        </div>
      </div>

      <!-- 全屏切换 -->
      <div class="action-item" @click="toggleFullScreen">
        <div class="icon-wrapper">
          <svg v-if="!isFullScreen" class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 3H5a2 2 0 0 0-2 2v3"/>
            <path d="M21 8V5a2 2 0 0 0-2-2h-3"/>
            <path d="M3 16v3a2 2 0 0 0 2 2h3"/>
            <path d="M16 21h3a2 2 0 0 0 2-2v-3"/>
          </svg>
          <svg v-else class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 3v3a2 2 0 0 1-2 2H3"/>
            <path d="M21 8h-3a2 2 0 0 1-2-2V3"/>
            <path d="M3 16h3a2 2 0 0 1 2 2v3"/>
            <path d="M16 21v-3a2 2 0 0 1 2-2h3"/>
          </svg>
        </div>
      </div>

      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <div class="user-avatar-wrapper">
            <div class="user-avatar">
              {{ userStore.realName?.charAt(0) || 'U' }}
            </div>
          </div>
          <span class="user-name">{{ userStore.realName || userStore.username }}</span>
          <svg class="user-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6,9 12,15 18,9"/>
          </svg>
        </div>

        <template #dropdown>
          <el-dropdown-menu class="header-dropdown-menu">
            <el-dropdown-item command="profile">
              <svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
              </svg>
              账号设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16,17 21,12 16,7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
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
 * - 快捷操作（搜索、通知、全屏）
 */
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
const notificationCount = ref(5)
const isFullScreen = ref(false)

// 当前页面标题
const currentTitle = computed(() => {
  return (route.meta.title as string) || 'DRP DevOps'
})

// 下拉菜单命令
async function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      break
    case 'settings':
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
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
    isFullScreen.value = true
  } else {
    document.exitFullscreen()
    isFullScreen.value = false
  }
}

// 通知点击
function handleNotification() {
  notificationCount.value = 0
}
</script>

<style lang="scss" scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 24px;
  height: 100%;
  box-sizing: border-box;
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
  gap: 16px;
}

// 搜索框
.search-wrapper {
  .header-search {
    width: 200px;
    transition: all 0.3s ease;

    &:hover,
    &:focus-within {
      width: 240px;
      box-shadow: 0 2px 12px rgba(102, 126, 234, 0.15);
    }

    :deep(.el-input__wrapper) {
      border-radius: 20px;
      padding: 0 16px;
      height: 36px;
      background: #f5f7fa;
      border: 1px solid transparent;
      box-shadow: none;
      transition: all 0.3s ease;

      &:hover {
        background: #fff;
        border-color: #667eea;
      }

      &.is-focus {
        background: #fff;
        border-color: #667eea;
        box-shadow: 0 2px 12px rgba(102, 126, 234, 0.15);
      }
    }

    :deep(.el-input__inner) {
      &::placeholder {
        color: #909399;
      }
    }
  }

  .search-icon {
    width: 14px;
    height: 14px;
    color: #909399;
  }
}

// 操作项
.action-item {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  .icon-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(102, 126, 234, 0.08);
    }
  }

  .action-icon {
    width: 18px;
    height: 18px;
    color: #606266;
    transition: color 0.3s ease;
  }

  &:hover .action-icon {
    color: #667eea;
  }

  .badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
    background: #f56c6c;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    line-height: 1;
  }
}

// 用户信息
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(102, 126, 234, 0.08);
  }
}

.user-avatar-wrapper {
  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-weight: 600;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.3s ease;

    &:hover {
      transform: scale(1.08);
    }
  }
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-arrow {
  width: 14px;
  height: 14px;
  color: #909399;
}

// 下拉菜单样式
:deep(.header-dropdown-menu) {
  padding: 6px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: none;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    border-radius: 6px;
    margin: 2px 0;
    font-size: 14px;
    color: #606266;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(102, 126, 234, 0.08);
      color: #667eea;
    }

    &.is-divided {
      margin-top: 6px;
      border-top: 1px solid #ebeef5;
      padding-top: 10px;
    }
  }

  .dropdown-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }
}

// 响应式
@media (max-width: 768px) {
  .search-wrapper {
    .header-search {
      width: 160px;

      &:hover,
      &:focus-within {
        width: 180px;
      }
    }
  }

  .user-name {
    display: none;
  }

  .header-right {
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .search-wrapper {
    .header-search {
      width: 120px;

      &:hover,
      &:focus-within {
        width: 140px;
      }
    }
  }
}
</style>
