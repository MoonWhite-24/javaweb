<template>
  <DefaultLayout>
    <div class="cart-page">
      <h2>购物车</h2>
      <el-table v-if="cartStore.items.length" :data="cartStore.items" style="width:100%">
        <el-table-column prop="name" label="商品" />
        <el-table-column prop="price" label="单价">
          <template #default="{ row }">&yen;{{ (row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="row.stock || 999" size="small" @change="cartStore.updateQty(row.productId, row.quantity)" />
          </template>
        </el-table-column>
        <el-table-column label="小计">
          <template #default="{ row }">&yen;{{ ((row.price || 0) * (row.quantity || 1)).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="cartStore.remove(row.productId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty">购物车为空</div>
      <div v-if="cartStore.items.length" class="cart-footer">
        <span class="total">合计: &yen;{{ cartStore.totalPrice.toFixed(2) }}</span>
        <el-button type="primary" @click="checkout">去结算</el-button>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { useCartStore } from '../../stores/cart'

const cartStore = useCartStore()
const router = useRouter()

onMounted(() => cartStore.fetch())

const checkout = async () => {
  const { createOrder } = await import('../../api/order')
  const { ElMessage } = await import('element-plus')
  const { data } = await createOrder(0)
  if (data.code === 200) {
    router.push(`/orders/pay/${data.data.orderNo}`)
  } else {
    ElMessage.error(data.msg || '创建订单失败')
  }
}
</script>

<style scoped>
.cart-page { max-width: 1000px; margin: 40px auto; padding: 20px; }
.cart-page h2 { margin-bottom: 20px; }
.empty { text-align: center; padding: 60px; color: #909399; }
.cart-footer { display: flex; justify-content: flex-end; align-items: center; margin-top: 20px; gap: 16px; }
.total { font-size: 20px; font-weight: bold; color: #f56c6c; }
</style>
