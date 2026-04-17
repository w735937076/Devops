/**
 * DRP Platform - 应用状态管理
 *
 * @author Nick
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// =====================================================
// 创建 Store
// =====================================================

export const useAppStore = defineStore('app', () => {
  // =====================================================
  // State
  // =====================================================

  /** 侧边栏折叠状态 */
  const sidebarCollapsed = ref(false)

  /** 侧边栏展开宽度 */
  const sidebarExpandedWidth = 200

  /** 侧边栏折叠宽度 */
  const sidebarCollapsedWidth = 64

  /** 移动端模式 */
  const isMobile = ref(window.innerWidth < 768)

  /** 全局加载状态 */
  const loading = ref(false)

  /** 全局加载提示文本 */
  const loadingText = ref('加载中...')

  // =====================================================
  // Getters
  // =====================================================

  /** 侧边栏当前宽度 */
  const sidebarWidth = computed(() =>
    sidebarCollapsed.value ? sidebarCollapsedWidth : sidebarExpandedWidth
  )

  // =====================================================
  // Actions
  // =====================================================

  /**
   * 切换侧边栏折叠状态
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
    // 持久化
    localStorage.setItem('sidebarCollapsed', String(sidebarCollapsed.value))
  }

  /**
   * 设置侧边栏折叠状态
   */
  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
    localStorage.setItem('sidebarCollapsed', String(collapsed))
  }

  /**
   * 初始化侧边栏状态（从本地存储恢复）
   */
  function initSidebarState() {
    const saved = localStorage.getItem('sidebarCollapsed')
    if (saved !== null) {
      sidebarCollapsed.value = saved === 'true'
    }
  }

  /**
   * 设置全局加载状态
   */
  function setLoading(show: boolean, text: string = '加载中...') {
    loading.value = show
    loadingText.value = text
  }

  /**
   * 处理窗口尺寸变化
   */
  function handleResize() {
    isMobile.value = window.innerWidth < 768
    if (isMobile.value) {
      sidebarCollapsed.value = true
    }
  }

  // =====================================================
  // 返回
  // =====================================================

  return {
    // State
    sidebarCollapsed,
    sidebarExpandedWidth,
    sidebarCollapsedWidth,
    isMobile,
    loading,
    loadingText,
    // Getters
    sidebarWidth,
    // Actions
    toggleSidebar,
    setSidebarCollapsed,
    initSidebarState,
    setLoading,
    handleResize
  }
})
