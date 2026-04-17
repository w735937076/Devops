<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">构建记录</h2>
    </div>

    <div class="card-container">
      <el-table :data="[]" stripe>
        <el-table-column prop="projectName" label="项目" />
        <el-table-column prop="branch" label="分支" />
        <el-table-column prop="commitId" label="Commit ID" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="duration" label="耗时" />
        <el-table-column prop="createTime" label="构建时间" />
      </el-table>
    </div>
  </div>
</template>
