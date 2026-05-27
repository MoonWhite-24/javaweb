<template>
  <DefaultLayout>
    <div class="order-page">
      <h2>我的订单</h2>
      <el-table v-if="orders.length" :data="orders" style="width:100%">
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="totalAmount" label="金额">
          <template #default="{ row }">&yen;{{ (row.totalAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'">
              {{ row.status === 1 ? '已支付' : row.status === 0 ? '待支付' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/orders/${row.orderNo}`)">详情</el-button>
            <el-button v-if="row.status === 0" size="small" type="primary" @click="$router.push(`/orders/pay/${row.orderNo}`)">支付</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty">暂无订单</div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { getOrders } from '../../api/order'

const orders = ref([])

onMounted(async () => {
  const { data } = await getOrders({ pageNum: 1, pageSize: 50 })
  if (data.code === 200) orders.value = data.data.list || data.data.records || []
})
</script>

<style scoped>
.order-page { max-width: 1000px; margin: 40px auto; padding: 20px; }
.order-page h2 { margin-bottom: 20px; }
.empty { text-align: center; padding: 60px; color: #909399; }
</style>
