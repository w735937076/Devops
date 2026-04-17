<!--
  DRP Platform - 全屏布局

  用于需要全屏展示的页面（如：日志监控、构建监控）

  @author Nick
-->
<template>
  <div class="fullscreen-layout">
    <router-view />
  </div>
</template>

<style lang="scss" scoped>
.fullscreen-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #1e1e1e;
}
</style>
