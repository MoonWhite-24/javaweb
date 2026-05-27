<template>
  <AdminLayout>
    <div class="admin-users">
      <h2>用户管理</h2>
      <el-table :data="users" border style="margin-top:20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
      </el-table>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminUsers } from '../../api/admin'

const users = ref([])

onMounted(async () => {
  const { data } = await getAdminUsers()
  if (data.code === 200) users.value = data.data || []
})
</script>

<style scoped>
.admin-users { padding: 20px; }
</style>
