<template>
  <DefaultLayout>
    <div class="pay-result">
      <el-result :icon="paid ? 'success' : 'warning'" :title="paid ? '支付成功' : '待支付'">
        <template #extra>
          <el-button type="primary" @click="handlePay" :disabled="paid">确认支付</el-button>
          <el-button @click="$router.push('/orders')">查看订单</el-button>
        </template>
      </el-result>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { payOrder } from '../../api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const paid = ref(false)

const handlePay = async () => {
  const orderNo = Number(route.params.orderNo)
  const { data } = await payOrder(orderNo, 0, 'PAY' + orderNo)
  if (data.code === 200) { paid.value = true; ElMessage.success('支付成功') }
  else ElMessage.error(data.msg || '支付失败')
}
</script>

<style scoped>
.pay-result { max-width: 600px; margin: 60px auto; }
</style>
