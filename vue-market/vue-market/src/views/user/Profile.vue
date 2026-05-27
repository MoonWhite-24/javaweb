<template>
  <DefaultLayout>
    <div class="profile-page">
      <el-card>
        <h2>个人中心</h2>
        <el-descriptions v-if="user" :column="1" border>
          <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ user.role === 1 ? '管理员' : '普通用户' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import request from '../../api/request'

const user = ref(null)

onMounted(async () => {
  const { data } = await request.get('/users/profile')
  if (data.code === 200) user.value = data.data
})
</script>

<style scoped>
.profile-page { max-width: 600px; margin: 40px auto; padding: 20px; }
</style>
