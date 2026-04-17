<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">项目管理</h2>
      <el-button type="primary" @click="handleCreate">创建项目</el-button>
    </div>

    <div class="card-container">
      <el-table :data="[]" stripe>
        <el-table-column prop="name" label="项目名称" />
        <el-table-column prop="code" label="项目编码" />
        <el-table-column prop="gitUrl" label="Git仓库" />
        <el-table-column prop="status" label="状态">
          <template #default>
            <el-tag type="success">启用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
function handleCreate() {
  // TODO
}
</script>
