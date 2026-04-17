<!--
  DRP Platform - 表单组件封装

  功能：
  - 统一表单样式
  - 响应式布局
  - 内置提交/重置按钮

  @author Nick
-->
<template>
  <div class="drp-form">
    <el-form
      ref="formRef"
      :model="model"
      :rules="rules"
      :label-width="labelWidth"
      :label-position="labelPosition"
      :size="size"
      :disabled="disabled"
    >
      <el-row :gutter="gutter">
        <slot />
      </el-row>
    </el-form>

    <!-- 操作按钮 -->
    <div v-if="showActions" class="form-actions">
      <el-button @click="handleReset">{{ resetText }}</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ submitText }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 表单组件
 *
 * Props:
 * - model: 表单数据
 * - rules: 表单验证规则
 * - labelWidth: 标签宽度
 * - labelPosition: 标签位置
 * - size: 表单尺寸
 * - showActions: 是否显示操作按钮
 * - submitText: 提交按钮文本
 * - resetText: 重置按钮文本
 */
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  model?: Record<string, any>
  rules?: FormRules
  labelWidth?: string | number
  labelPosition?: 'left' | 'right' | 'top'
  size?: 'large' | 'default' | 'small'
  disabled?: boolean
  gutter?: number
  showActions?: boolean
  submitText?: string
  resetText?: string
  submitting?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  model: () => ({}),
  rules: () => ({}),
  labelWidth: '120px',
  labelPosition: 'right',
  size: 'default',
  disabled: false,
  gutter: 20,
  showActions: true,
  submitText: '提交',
  resetText: '重置',
  submitting: false
})

const emit = defineEmits<{
  submit: [model: Record<string, any>]
  reset: []
}>()

const formRef = ref<FormInstance>()

/**
 * 表单验证
 */
async function validate(): Promise<boolean> {
  return formRef.value?.validate() ?? false
}

/**
 * 表单重置
 */
function reset() {
  formRef.value?.resetFields()
}

/**
 * 清除验证
 */
function clearValidate() {
  formRef.value?.clearValidate()
}

/**
 * 提交表单
 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (valid) {
    emit('submit', props.model)
  }
}

/**
 * 重置表单
 */
function handleReset() {
  reset()
  emit('reset')
}

// 暴露方法
defineExpose({
  validate,
  reset,
  clearValidate
})

import { ref } from 'vue'
</script>

<style lang="scss" scoped>
.drp-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  margin-top: 8px;
}
</style>
