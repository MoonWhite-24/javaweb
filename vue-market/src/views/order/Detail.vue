<template>
  <DefaultLayout>
    <div class="order-detail" v-if="order">
      <h2>订单详情 #{{ order.orderNo }}</h2>
      <el-descriptions :column="2" border style="margin-bottom: 24px">
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="金额">&yen;{{ (order.totalPrice || order.payment || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="order.status === 1 ? 'success' : order.status === 0 ? 'warning' : 'info'">
            {{ order.status === 1 ? '已支付' : order.status === 0 ? '待支付' : order.status === 5 ? '已取消' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="order.paymentTime" label="支付时间">{{ order.paymentTime }}</el-descriptions-item>
        <el-descriptions-item v-if="order.remark" label="备注">{{ order.remark }}</el-descriptions-item>
      </el-descriptions>

      <h3 v-if="order.items && order.items.length">订单商品</h3>
      <el-table v-if="order.items && order.items.length" :data="order.items" style="width:100%">
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <img :src="productImage(row.productImage)" class="item-img" @error="usePlaceholderImage" />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column label="单价">
          <template #default="{ row }">&yen;{{ (row.unitPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="60" />
        <el-table-column label="小计">
          <template #default="{ row }">&yen;{{ (row.totalPrice || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { getOrderDetail } from '../../api/order'
import { productImage, usePlaceholderImage } from '../../utils/image'

const route = useRoute()
const order = ref(null)

onMounted(async () => {
  const { data } = await getOrderDetail(route.params.orderNo)
  if (data.code === 200) order.value = data.data
})
</script>

<style scoped>
.order-detail { max-width: 900px; margin: 40px auto; padding: 20px; }
.order-detail h2 { margin-bottom: 20px; }
.order-detail h3 { margin: 24px 0 12px; }
.item-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
</style>
