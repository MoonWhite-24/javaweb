<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>管理员登录</h2>
      <el-form @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="password" type="password" placeholder="密码" show-password /></el-form-item>
        <el-form-item><el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登录</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  const res = await userStore.login(username.value, password.value)
  loading.value = false
  if (res.code === 200) {
    if (userStore.isAdmin) router.push('/admin')
    else ElMessage.error('非管理员账号')
  } else {
    ElMessage.error(res.msg || '登录失败')
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f5f7fa; }
.auth-card { width: 400px; }
.auth-card h2 { text-align: center; margin-bottom: 24px; }
</style>
