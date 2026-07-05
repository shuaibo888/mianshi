<template>
  <router-view v-if="$route.path === '/login'" />
  <el-container v-else class="app-shell">
    <el-aside width="224px" class="sidebar">
      <div class="brand">
        <div class="brand-mark">ATS</div>
        <div>
          <strong>Mini ATS</strong>
          <span>招聘跟进系统</span>
        </div>
      </div>
      <el-menu :default-active="$route.path" router class="side-menu">
        <el-menu-item index="/">
          <el-icon><User /></el-icon>
          <span>候选人</span>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计看板</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="account">
          <span>{{ auth.displayName || auth.username }}</span>
          <el-button text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataAnalysis, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const pageTitle = computed(() => {
  if (route.path.startsWith('/dashboard')) return '统计看板'
  if (route.path.startsWith('/candidates/')) return '候选人详情'
  return '候选人管理'
})

function logout() {
  auth.logout()
  router.replace('/login')
}
</script>
