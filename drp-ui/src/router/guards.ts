/**
 * DRP Platform - 路由守卫
 *
 * 功能：
 * - 路由鉴权
 * - 权限校验
 * - 页面标题更新
 *
 * @author Nick
 */

import type { Router } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// =====================================================
// 白名单（无需登录即可访问的路由）
// =====================================================

const whiteList = ['/login', '/404', '/403']

// =====================================================
// 设置路由守卫
// =====================================================

export function setupRouterGuard(router: Router) {
  // 全局前置守卫
  router.beforeEach(async (to, from, next) => {
    // 设置页面标题
    const title = to.meta.title as string
    document.title = title ? `${title} - DRP DevOps` : 'DRP DevOps Platform'

    // 检查是否在白名单中
    const isPublicPage = to.meta.public === true || whiteList.includes(to.path)

    // 获取 Token
    const userStore = useUserStore()
    const hasToken = !!userStore.token

    if (isPublicPage) {
      // 公开页面直接放行
      next()
    } else if (hasToken) {
      // 已登录，检查是否有用户信息
      if (!userStore.userInfo) {
        try {
          // 获取用户信息
          await userStore.fetchUserInfo()
          next()
        } catch {
          // 获取失败，清除 Token 并跳转登录
          userStore.logout()
          next('/login')
        }
      } else {
        next()
      }
    } else {
      // 未登录，跳转到登录页
      next('/login')
    }
  })

  // 全局后置守卫
  router.afterEach((to) => {
    // 路由切换后的滚动处理
    if (to.hash) {
      const element = document.querySelector(to.hash)
      if (element) {
        element.scrollIntoView()
      }
    } else {
      window.scrollTo(0, 0)
    }
  })
}
