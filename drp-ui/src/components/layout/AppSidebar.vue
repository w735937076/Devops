<!--
  DRP Platform - 侧边栏组件

  @author Nick
-->
<template>
  <div class="app-sidebar">
    <!-- Logo 区域 -->
    <div class="sidebar-logo">
      <router-link to="/" class="logo-link">
        <img src="/favicon.svg" alt="DRP" class="logo-icon" />
        <span v-if="!appStore.sidebarCollapsed" class="logo-text">DRP</span>
      </router-link>
    </div>

    <!-- 菜单导航 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      :collapse-transition="false"
      background-color="transparent"
      text-color="#b8bcc8"
      active-text-color="#fff"
      class="sidebar-menu"
    >
      <template v-for="item in menuList" :key="item.path">
        <!-- 有子菜单 -->
        <el-sub-menu v-if="item.children?.length" :index="item.path">
          <template #title>
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.path"
            :index="child.path"
            @click="handleMenuClick(child)"
          >
            <el-icon>
              <component :is="child.icon" />
            </el-icon>
            <span>{{ child.title }}</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 无子菜单 -->
        <el-menu-item v-else :index="item.path" @click="handleMenuClick(item)">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>

    <!-- 底部折叠按钮 -->
    <div class="sidebar-footer">
      <el-button
        :icon="appStore.sidebarCollapsed ? 'Expand' : 'Fold'"
        text
        class="collapse-btn"
        @click="appStore.toggleSidebar"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 侧边栏组件
 *
 * 功能：
 * - 动态菜单渲染
 * - 菜单高亮
 * - 折叠/展开
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

// Store
const appStore = useAppStore()
const route = useRoute()
const router = useRouter()

// 菜单配置
interface MenuItem {
  path: string
  title: string
  icon: string
  children?: MenuItem[]
}

const menuList: MenuItem[] = [
  {
    path: '/dashboard',
    title: '首页',
    icon: 'Odometer'
  },
  {
    path: '/projects',
    title: '项目管理',
    icon: 'FolderOpened'
  },
  {
    path: '/credentials',
    title: '凭证管理',
    icon: 'Key'
  },
  {
    path: '/builds',
    title: '构建记录',
    icon: 'Box'
  },
  {
    path: '/deploys',
    title: '部署记录',
    icon: 'Upload'
  },
  {
    path: '/servers',
    title: '服务器',
    icon: 'Monitor'
  },
  {
    path: '/logs',
    title: '日志中心',
    icon: 'Document'
  },
  {
    path: '/system',
    title: '系统管理',
    icon: 'Setting',
    children: [
      { path: '/system/users', title: '用户管理', icon: 'User' },
      { path: '/system/roles', title: '角色管理', icon: 'Key' },
      { path: '/system/permissions', title: '权限管理', icon: 'Lock' }
    ]
  }
]

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  // 精确匹配
  if (menuList.some((m) => m.path === path)) {
    return path
  }
  // 子菜单匹配
  for (const item of menuList) {
    if (item.children?.some((c) => c.path === path)) {
      return item.path
    }
  }
  return path
})

// 菜单点击
function handleMenuClick(item: MenuItem) {
  router.push(item.path)
}
</script>

<style lang="scss" scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

// Logo 区域
.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.logo-icon {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
}

// 菜单
.sidebar-menu {
  flex: 1;
  border-right: none;

  &:not(.el-menu--collapse) {
    width: 200px;
  }

  .el-menu-item,
  :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;
    margin: 4px 8px;
    border-radius: 8px;

    &:hover {
      background: rgba(255, 255, 255, 0.1) !important;
    }

    &.is-active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
    }
  }

  .el-icon {
    margin-right: 10px;
  }
}

// 底部
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.collapse-btn {
  width: 100%;
  color: #b8bcc8;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.1) !important;
  }
}
</style>
