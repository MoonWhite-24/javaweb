<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>注册</h2>
      <el-form @submit.prevent="handleRegister">
        <el-form-item><el-input v-model="username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="password" type="password" placeholder="密码" show-password /></el-form-item>
        <el-form-item><el-input v-model="password2" type="password" placeholder="确认密码" show-password /></el-form-item>
        <el-form-item><el-button type="primary" native-type="submit" :loading="loading" style="width:100%">注册</el-button></el-form-item>
      </el-form>
      <p style="text-align:center"><router-link to="/login">已有账号？立即登录</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const username = ref('')
const password = ref('')
const password2 = ref('')
const loading = ref(false)

const handleRegister = async () => {
  if (password.value !== password2.value) { ElMessage.error('两次密码不一致'); return }
  loading.value = true
  const res = await userStore.register(username.value, password.value)
  loading.value = false
  if (res.code !== 200) ElMessage.error(res.msg || '注册失败')
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f5f7fa; }
.auth-card { width: 400px; }
.auth-card h2 { text-align: center; margin-bottom: 24px; }
</style>
