<template>
  <div class="dashboard" v-loading="loading">
    <section class="stats-grid" v-if="stats">
      <div class="metric big">
        <span>总候选人</span>
        <strong>{{ stats.totalCandidates }}</strong>
      </div>
      <div class="metric big">
        <span>平均评分</span>
        <strong>{{ stats.averageRating }}</strong>
      </div>
      <div class="metric big">
        <span>录用率</span>
        <strong>{{ stats.hireRate }}%</strong>
      </div>
    </section>

    <section class="panel">
      <div class="section-head compact">
        <h3>各状态人数</h3>
      </div>
      <el-table :data="rows" empty-text="暂无统计数据">
        <el-table-column prop="label" label="状态" />
        <el-table-column prop="count" label="人数" />
        <el-table-column label="占比">
          <template #default="{ row }">
            <el-progress :percentage="row.percent" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getDashboardStats } from '@/api/dashboard'
import { getOptions } from '@/api/candidates'
import type { DashboardStats, OptionItem } from '@/types'

const loading = ref(false)
const stats = ref<DashboardStats>()
const statuses = ref<OptionItem[]>([])

const rows = computed(() => {
  if (!stats.value) return []
  return statuses.value.map((status) => {
    const count = stats.value?.statusCounts[status.value] ?? 0
    const percent = stats.value?.totalCandidates
      ? Math.round((count / stats.value.totalCandidates) * 100)
      : 0
    return { label: status.label, count, percent }
  })
})

onMounted(async () => {
  loading.value = true
  try {
    const [options, data] = await Promise.all([getOptions(), getDashboardStats()])
    statuses.value = options.statuses
    stats.value = data
  } finally {
    loading.value = false
  }
})
</script>
