<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">服务器管理</h2>
      <el-button type="primary">添加服务器</el-button>
    </div>

    <div class="card-container">
      <el-table :data="[]" stripe>
        <el-table-column prop="name" label="服务器名称" />
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column prop="port" label="端口" />
        <el-table-column prop="env" label="环境" />
        <el-table-column prop="status" label="状态">
          <template #default>
            <el-tag type="success">在线</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="info">测试</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
