<!--
  DRP Platform - 用户登录页面

  @author Nick
-->
<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1" />
      <div class="bg-circle bg-circle-2" />
      <div class="bg-circle bg-circle-3" />
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo 区域 -->
      <div class="login-header">
        <img src="/favicon.svg" alt="DRP" class="login-logo" />
        <h1 class="login-title">DRP DevOps Platform</h1>
        <p class="login-subtitle">自动化部署与运维平台</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 记住我 & 忘记密码 -->
        <div class="form-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <a href="#" class="forgot-link">忘记密码？</a>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部信息 -->
      <div class="login-footer">
        <p>默认账号：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 用户登录页面
 *
 * 功能：
 * - 用户名密码登录
 * - 表单验证
 * - 错误处理
 * - 登录成功跳转
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

// Store
const userStore = useUserStore()
const router = useRouter()

// Refs
const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为 6-30 个字符', trigger: 'blur' }
  ]
}

/**
 * 处理登录
 */
async function handleLogin() {
  // 表单验证
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  try {
    // 执行登录
    await userStore.login(form)

    ElMessage.success('登录成功')

    // 跳转到首页
    router.push('/')
  } catch (error: any) {
    // 错误已由 Axios 拦截器处理
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

// 背景装饰
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);

  &-1 {
    width: 400px;
    height: 400px;
    top: -100px;
    right: -100px;
  }

  &-2 {
    width: 300px;
    height: 300px;
    bottom: -50px;
    left: -50px;
  }

  &-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    left: 10%;
  }
}

// 登录卡片
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

// Logo 区域
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

// 表单
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    padding: 4px 16px;
    border-radius: 8px;
  }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 14px;
  color: #667eea;

  &:hover {
    color: #764ba2;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;

  &:hover {
    background: linear-gradient(135deg, #5568d0 0%, #5f3d94 100%);
  }
}

// 底部信息
.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;

  p {
    margin: 0;
    font-size: 12px;
    color: #c0c4cc;
  }
}
</style>
