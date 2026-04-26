/**
 * DRP Platform - 应用入口文件
 *
 * @author Nick
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

// 导入全局样式
import 'element-plus/dist/index.css'
import '@/assets/styles/global.scss'

// =====================================================
// 创建应用实例
// =====================================================

const app = createApp(App)

// =====================================================
// 注册 Element Plus 图标
// =====================================================

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// =====================================================
// 使用插件
// =====================================================

app.use(createPinia()) // Pinia 状态管理
app.use(router) // Vue Router
app.use(ElementPlus, {
  locale: zhCn as any, // 中文语言包
  size: 'default' // 默认尺寸
})

// =====================================================
// 挂载应用
// =====================================================

app.mount('#app')
