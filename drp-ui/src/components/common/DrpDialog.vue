<!--
  DRP Platform - 弹窗组件封装

  功能：
  - 统一弹窗样式
  - 支持全屏
  - 确认/取消操作

  @author Nick
-->
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="isFullscreen"
    :append-to-body="true"
    :destroy-on-close="destroyOnClose"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 内容插槽 -->
    <slot />

    <!-- 底部操作 -->
    <template #footer v-if="showFooter">
      <slot name="footer">
        <el-button @click="handleCancel">{{ cancelText }}</el-button>
        <el-button type="primary" :loading="confirming" @click="handleConfirm">
          {{ confirmText }}
        </el-button>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

/**
 * 弹窗组件
 *
 * Props:
 * - modelValue: 双向绑定
 * - title: 标题
 * - width: 宽度
 * - fullscreen: 是否全屏
 * - showFooter: 是否显示底部
 * - confirmText: 确认按钮文本
 * - cancelText: 取消按钮文本
 * - confirming: 确认按钮加载状态
 */
interface Props {
  modelValue?: boolean
  title?: string
  width?: string
  fullscreen?: boolean
  destroyOnClose?: boolean
  showFooter?: boolean
  confirmText?: string
  cancelText?: string
  confirming?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: '',
  width: '600px',
  fullscreen: false,
  destroyOnClose: true,
  showFooter: true,
  confirmText: '确定',
  cancelText: '取消',
  confirming: false
})

const emit = defineEmits<{
  'update:modelValue': [visible: boolean]
  confirm: []
  cancel: []
  close: []
}>()

// 全屏状态
const isFullscreen = ref(props.fullscreen)

watch(
  () => props.fullscreen,
  (val) => {
    isFullscreen.value = val
  }
)

// 双向绑定
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/**
 * 关闭弹窗
 */
function handleClose() {
  emit('close')
}

/**
 * 确认操作
 */
function handleConfirm() {
  emit('confirm')
}

/**
 * 取消操作
 */
function handleCancel() {
  visible.value = false
  emit('cancel')
}
</script>

<style lang="scss" scoped>
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;

  .el-dialog__header {
    padding: 20px 24px;
    margin: 0;
    border-bottom: 1px solid #ebeef5;
    background: #fafafa;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #ebeef5;
    background: #fafafa;
  }
}
</style>
