/**
 * DRP Platform - 路由配置
 *
 * @author Nick
 */

import type { RouteRecordRaw } from 'vue-router'

// =====================================================
// 路由配置
// =====================================================

const routes: RouteRecordRaw[] = [
  // =====================================================
  // 公共路由（无需登录）
  // =====================================================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', public: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', public: true }
  },

  // =====================================================
  // 业务路由（需登录）
  // =====================================================
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      // 首页
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '首页', icon: 'Odometer' }
      },

      // 项目管理
      {
        path: 'projects',
        name: 'ProjectList',
        component: () => import('@/views/project/List.vue'),
        meta: { title: '项目管理', icon: 'FolderOpened' }
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/Detail.vue'),
        meta: { title: '项目详情', hidden: true }
      },

      // 凭证管理
      {
        path: 'credentials',
        name: 'CredentialList',
        component: () => import('@/views/credential/List.vue'),
        meta: { title: '凭证管理', icon: 'Key' }
      },

      // 构建管理
      {
        path: 'builds',
        name: 'BuildList',
        component: () => import('@/views/build/List.vue'),
        meta: { title: '构建记录', icon: 'Box' }
      },
      {
        path: 'builds/:id',
        name: 'BuildDetail',
        component: () => import('@/views/build/Detail.vue'),
        meta: { title: '构建详情', hidden: true }
      },

      // 部署管理
      {
        path: 'deploys',
        name: 'DeployList',
        component: () => import('@/views/deploy/List.vue'),
        meta: { title: '部署记录', icon: 'Upload' }
      },
      {
        path: 'deploys/create',
        name: 'DeployCreate',
        component: () => import('@/views/deploy/Create.vue'),
        meta: { title: '创建部署', hidden: true }
      },
      {
        path: 'deploys/:id',
        name: 'DeployDetail',
        component: () => import('@/views/deploy/Detail.vue'),
        meta: { title: '部署详情', hidden: true }
      },

      // 服务器管理
      {
        path: 'servers',
        name: 'ServerList',
        component: () => import('@/views/server/List.vue'),
        meta: { title: '服务器', icon: 'Monitor' }
      },

      // 日志中心
      {
        path: 'logs',
        name: 'LogCenter',
        component: () => import('@/views/log/Index.vue'),
        meta: { title: '日志中心', icon: 'Document' }
      },

      // 系统管理
      {
        path: 'system/users',
        name: 'UserManagement',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/roles',
        name: 'RoleManagement',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', icon: 'Key' }
      },
      {
        path: 'system/permissions',
        name: 'PermissionManagement',
        component: () => import('@/views/system/Permission.vue'),
        meta: { title: '权限管理', icon: 'Lock' }
      }
    ]
  },

  // =====================================================
  // 空布局（全屏页面）
  // =====================================================
  {
    path: '/blank',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: []
  },

  // =====================================================
  // 捕获所有路由（重定向到 404）
  // =====================================================
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

export default routes
