<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">部署记录</h2>
      <el-button type="primary" @click="$router.push('/deploys/create')">创建部署</el-button>
    </div>

    <div class="card-container">
      <el-table :data="[]" stripe>
        <el-table-column prop="projectName" label="项目" />
        <el-table-column prop="version" label="版本" />
        <el-table-column prop="env" label="环境" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="deployUser" label="部署人" />
        <el-table-column prop="createTime" label="部署时间" />
      </el-table>
    </div>
  </div>
</template>
