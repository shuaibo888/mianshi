<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <div class="brand-mark large">ATS</div>
        <h1>Mini ATS</h1>
        <p>招聘候选人跟进系统</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" size="large" placeholder="账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" size="large" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" native-type="submit" class="full-button">
          登录
        </el-button>
        <div class="demo-account">测试账号：admin / admin123456</div>
      </el-form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123456',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.replace('/')
  } finally {
    loading.value = false
  }
}
</script>
