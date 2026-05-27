<template>
  <DefaultLayout>
    <div class="order-detail" v-if="order">
      <h2>订单详情 #{{ order.orderNo }}</h2>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="金额">&yen;{{ (order.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="order.status === 1 ? 'success' : 'warning'">{{ order.status === 1 ? '已支付' : '待支付' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { getOrderDetail } from '../../api/order'

const route = useRoute()
const order = ref(null)

onMounted(async () => {
  const { data } = await getOrderDetail(route.params.orderNo)
  if (data.code === 200) order.value = data.data
})
</script>

<style scoped>
.order-detail { max-width: 800px; margin: 40px auto; padding: 20px; }
.order-detail h2 { margin-bottom: 20px; }
</style>
