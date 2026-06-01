<template>
  <DefaultLayout>
    <div class="profile-page">
      <h2>个人中心</h2>

      <!-- Info Card -->
      <el-card class="info-card">
        <template #header><span>基本信息</span><el-button type="primary" size="small" style="float:right" @click="startEdit">编辑</el-button></template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ user.role === 1 ? '管理员' : '普通用户' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(user.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Edit Dialog -->
      <el-dialog v-model="editVisible" title="编辑资料" width="420px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
        </template>
      </el-dialog>

      <!-- Change Password -->
      <el-card class="info-card">
        <template #header><span>修改密码</span></template>
        <el-form :model="pwdForm" label-width="100px" style="max-width:400px">
          <el-form-item label="当前密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
          <el-form-item><el-button type="primary" :loading="pwdLoading" @click="changePassword">修改密码</el-button></el-form-item>
        </el-form>
      </el-card>

      <!-- Delete Account -->
      <el-card class="info-card">
        <template #header><span style="color:#f56c6c">注销账号</span></template>
        <p style="color:#909399;margin-bottom:12px">注销后所有数据将被永久删除，不可恢复。</p>
        <el-button type="danger" @click="deleteAccount">注销账号</el-button>
      </el-card>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import request from '../../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const user = ref({})
const editVisible = ref(false)
const saving = ref(false)
const pwdLoading = ref(false)

const editForm = reactive({ phone: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const fetchProfile = async () => {
  const { data } = await request.get('/users/profile')
  if (data.code === 200) user.value = data.data
}

const startEdit = () => {
  editForm.phone = user.value.phone || ''
  editForm.email = user.value.email || ''
  editVisible.value = true
}

const saveProfile = async () => {
  saving.value = true
  try {
    const { data } = await request.put('/users/profile', {
      phone: editForm.phone || null,
      email: editForm.email || null
    })
    if (data.code === 200) {
      ElMessage.success('保存成功')
      editVisible.value = false
      await fetchProfile()
    } else {
      ElMessage.error(data.msg || '保存失败')
    }
  } finally { saving.value = false }
}

const changePassword = async () => {
  if (!pwdForm.oldPassword) { ElMessage.error('请输入当前密码'); return }
  if (!pwdForm.newPassword) { ElMessage.error('请输入新密码'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.error('两次密码不一致'); return }
  pwdLoading.value = true
  try {
    const { data } = await request.put('/users/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    if (data.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } else {
      ElMessage.error(data.msg || '修改失败')
    }
  } finally { pwdLoading.value = false }
}

const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm('确定要注销账号吗？此操作不可恢复，所有数据将被永久删除。', '注销账号', {
      confirmButtonText: '确认注销',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { data } = await request.delete('/users/account')
    if (data.code === 200) {
      ElMessage.success('账号已注销')
      localStorage.clear()
      router.push('/login')
    } else {
      ElMessage.error(data.msg || '注销失败')
    }
  } catch {}
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-page { max-width: 700px; margin: 40px auto; padding: 20px; }
.profile-page h2 { margin-bottom: 20px; }
.info-card { margin-bottom: 20px; }
</style>
