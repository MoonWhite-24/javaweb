<template>
  <AdminLayout>
    <h2>仪表盘</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6"><el-card><el-statistic title="秒杀商品数" :value="stats.seckillCount || 0" /></el-card></el-col>
    </el-row>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getStats } from '../../api/admin'

const stats = ref({})
onMounted(async () => {
  const { data } = await getStats()
  if (data.code === 200) stats.value = data.data
})
</script>
