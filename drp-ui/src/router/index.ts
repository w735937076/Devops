/**
 * DRP Platform - Vue Router 实例
 *
 * @author Nick
 */

import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
import { setupRouterGuard } from './guards'

// =====================================================
// 创建路由实例
// =====================================================

const router = createRouter({
  // 使用 HTML5 历史模式
  history: createWebHistory(import.meta.env.BASE_URL),
  // 路由配置
  routes,
  // 路由切换时滚动到顶部
  scrollBehavior() {
    return { top: 0 }
  }
})

// =====================================================
// 设置路由守卫
// =====================================================

setupRouterGuard(router)

// =====================================================
// 导出
// =====================================================

export default router
