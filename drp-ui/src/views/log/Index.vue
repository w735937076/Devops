<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">日志中心</h2>
    </div>

    <div class="card-container">
      <LogViewer :logs="logs" height="600px" />
    </div>
  </div>
</template>

<script setup lang="ts">
import LogViewer from '@/components/log/LogViewer.vue'

const logs = ref<string[]>([
  '[INFO] 2024-01-01 10:00:00 - Application started',
  '[INFO] 2024-01-01 10:00:01 - Loading configuration...',
  '[DEBUG] 2024-01-01 10:00:02 - Database connection established',
  '[WARN] 2024-01-01 10:00:03 - Cache miss for key: user_session_123',
  '[ERROR] 2024-01-01 10:00:04 - Failed to connect to external API',
  '[INFO] 2024-01-01 10:00:05 - Retrying connection...',
  '[INFO] 2024-01-01 10:00:06 - Connection restored'
])

import { ref } from 'vue'
</script>
