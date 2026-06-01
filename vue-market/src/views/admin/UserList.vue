<template>
  <AdminLayout>
    <div class="admin-users">
      <h2>用户管理</h2>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/手机号" clearable style="width:280px" @keyup.enter="doSearch" @clear="doSearch" />
        <el-button type="primary" @click="doSearch">搜索</el-button>
      </div>
      <el-table :data="users" border style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'">{{ row.status === 0 ? '已封禁' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.role !== 1 && row.status !== 0" size="small" type="danger" @click="banUser(row, 0)">封禁</el-button>
            <el-button v-if="row.role !== 1 && row.status === 0" size="small" type="success" @click="banUser(row, 1)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        style="margin-top:20px;justify-content:flex-end"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="search"
        @size-change="search"
      />
    </div>

    <!-- User Detail Dialog -->
    <el-dialog v-model="dialogVisible" title="用户详情" width="460px">
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="ID" :span="2">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ currentUser.role === 1 ? '管理员' : '普通用户' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentUser.status === 0 ? '已封禁' : '正常' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ formatTime(currentUser.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ formatTime(currentUser.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminUsers, getAdminUser, updateUserStatus } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentUser = ref(null)

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const showDetail = async (row) => {
  const { data } = await getAdminUser(row.id)
  if (data.code === 200) {
    currentUser.value = data.data
    dialogVisible.value = true
  }
}

const banUser = async (row, status) => {
  const action = status === 0 ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${row.username}」吗？`, `确认${action}`, { type: 'warning' })
    const { data } = await updateUserStatus(row.id, status)
    if (data.code === 200) {
      row.status = status
      ElMessage.success(`${action}成功`)
    } else {
      ElMessage.error(data.msg || `${action}失败`)
    }
  } catch {}
}

const doSearch = () => { pageNum.value = 1; search() }
const search = async () => {
  const params = { pageNum: pageNum.value, pageSize: pageSize.value }
  if (keyword.value) params.keyword = keyword.value
  const { data } = await getAdminUsers(params)
  if (data.code === 200) {
    users.value = (data.data && data.data.list) || data.data || []
    total.value = (data.data && data.data.total) || 0
  }
}

onMounted(() => search())
</script>

<style scoped>
.admin-users { padding: 20px; }
.toolbar { display: flex; gap: 10px; }
</style>
