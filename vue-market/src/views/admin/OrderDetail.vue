<template>
  <AdminLayout>
    <div class="admin-order-detail" v-if="order">
      <h2>订单详情 #{{ order.orderNo }}</h2>
      <el-descriptions :column="2" border style="margin-top:20px">
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ order.userId }}</el-descriptions-item>
        <el-descriptions-item label="金额">&yen;{{ (order.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="order.status === 1 ? 'success' : 'warning'">{{ order.status === 1 ? '已支付' : '待支付' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminOrder } from '../../api/admin'

const route = useRoute()
const order = ref(null)

onMounted(async () => {
  const { data } = await getAdminOrder(route.params.orderNo)
  if (data.code === 200) order.value = data.data
})
</script>

<style scoped>
.admin-order-detail { padding: 20px; }
</style>
