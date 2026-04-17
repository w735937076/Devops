<!--
  DRP Platform - 空白布局

  用于不需要导航栏的页面（如：登录页、全屏展示页）

  @author Nick
-->
<template>
  <div class="blank-layout">
    <router-view />
  </div>
</template>

<style lang="scss" scoped>
.blank-layout {
  height: 100vh;
  width: 100vw;
}
</style>
