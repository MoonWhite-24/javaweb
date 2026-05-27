<template>
  <AdminLayout>
    <div class="admin-orders">
      <h2>订单管理</h2>
      <el-table :data="orders" border style="margin-top:20px">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="totalAmount" label="金额">
          <template #default="{ row }">&yen;{{ (row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已支付' : '待支付' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/admin/orders/${row.orderNo}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminOrders } from '../../api/admin'

const orders = ref([])

onMounted(async () => {
  const { data } = await getAdminOrders({ pageNum: 1, pageSize: 50 })
  if (data.code === 200) orders.value = data.data.list || data.data.records || []
})
</script>

<style scoped>
.admin-orders { padding: 20px; }
</style>
