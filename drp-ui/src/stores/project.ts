/**
 * DRP Platform - 项目状态管理
 *
 * @author Nick
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Project } from '@/api/project'
import { getProjectList } from '@/api/project'

// =====================================================
// 创建 Store
// =====================================================

export const useProjectStore = defineStore('project', () => {
  // =====================================================
  // State
  // =====================================================

  /** 项目列表 */
  const projects = ref<Project[]>([])

  /** 当前选中的项目 */
  const currentProject = ref<Project | null>(null)

  /** 项目总数 */
  const total = ref(0)

  /** 加载状态 */
  const loading = ref(false)

  // =====================================================
  // Getters
  // =====================================================

  /** 项目选项（用于选择器） */
  const projectOptions = computed(() =>
    projects.value.map((p) => ({
      label: p.name,
      value: p.id
    }))
  )

  /** 项目 Map（快速查找） */
  const projectMap = computed(() => {
    const map = new Map<number, Project>()
    projects.value.forEach((p) => map.set(p.id, p))
    return map
  })

  // =====================================================
  // Actions
  // =====================================================

  /**
   * 获取项目列表
   */
  async function fetchProjects(params?: { keyword?: string; status?: number }) {
    loading.value = true
    try {
      const res = await getProjectList(params)
      projects.value = res.list
      total.value = res.total
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置当前项目
   */
  function setCurrentProject(project: Project | null) {
    currentProject.value = project
  }

  /**
   * 根据 ID 获取项目
   */
  function getProjectById(id: number): Project | undefined {
    return projectMap.value.get(id)
  }

  // =====================================================
  // 返回
  // =====================================================

  return {
    // State
    projects,
    currentProject,
    total,
    loading,
    // Getters
    projectOptions,
    projectMap,
    // Actions
    fetchProjects,
    setCurrentProject,
    getProjectById
  }
})
