<template>
  <AdminLayout>
    <div class="admin-order-detail" v-if="order">
      <div class="header">
        <h2>订单详情 #{{ order.orderNo }}</h2>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <!-- Order Info -->
      <el-descriptions :column="2" border style="margin-top:20px" title="基本信息">
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ order.userId }}</el-descriptions-item>
        <el-descriptions-item label="金额">&yen;{{ (order.totalPrice || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="实付">&yen;{{ (order.payment || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTags[order.status]">{{ statusLabels[order.status] || order.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ order.paymentType === 1 ? '在线支付' : order.paymentType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatTime(order.paymentTime) }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ formatTime(order.sendTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatTime(order.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消时间">{{ formatTime(order.closeTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- Shipping Info -->
      <el-descriptions v-if="order.shippingName" :column="2" border style="margin-top:20px" title="收货信息">
        <el-descriptions-item label="收货人">{{ order.shippingName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ order.shippingPhone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ order.shippingAddress }}</el-descriptions-item>
      </el-descriptions>

      <!-- Order Items -->
      <h3 style="margin-top:24px">订单商品</h3>
      <el-table v-if="order.items && order.items.length" :data="order.items" border style="width:100%">
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <img :src="row.productImage || ''" class="item-img" @error="e => e.target.style.display='none'" />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">&yen;{{ (row.unitPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">&yen;{{ (row.totalPrice || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-items">暂无商品明细</div>

      <!-- Actions -->
      <div class="actions" style="margin-top:24px">
        <el-button v-if="order.status === 0" type="success" @click="updateStatus(1)">标记已支付</el-button>
        <el-button v-if="order.status === 1" type="primary" @click="updateStatus(2)">标记已发货</el-button>
        <el-button v-if="order.status === 2" @click="updateStatus(4)">标记已完成</el-button>
        <el-button v-if="order.status === 0" type="danger" @click="updateStatus(5)">取消订单</el-button>
        <el-popconfirm title="确定删除该订单?" @confirm="del">
          <template #reference><el-button type="danger" plain>删除订单</el-button></template>
        </el-popconfirm>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminOrder, deleteAdminOrder, updateAdminOrderStatus } from '../../api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)

const statusLabels = ['待支付', '已支付', '已发货', '已收货', '已完成', '已取消']
const statusTags = ['info', 'success', '', '', '', 'danger']

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ')
}

const load = async () => {
  const { data } = await getAdminOrder(route.params.orderNo)
  if (data.code === 200) order.value = data.data
}

const updateStatus = async (status) => {
  const label = statusLabels[status]
  const { data } = await updateAdminOrderStatus(route.params.orderNo, status)
  if (data.code === 200) {
    ElMessage.success(`已${label}`)
    order.value.status = status
  } else {
    ElMessage.error(data.msg || '操作失败')
  }
}

const del = async () => {
  const { data } = await deleteAdminOrder(route.params.orderNo)
  if (data.code === 200) {
    ElMessage.success('删除成功')
    router.push('/admin/orders')
  } else {
    ElMessage.error(data.msg || '删除失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.admin-order-detail { padding: 20px; max-width: 1000px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.item-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.empty-items { text-align: center; padding: 40px; color: #909399; }
.actions { display: flex; gap: 10px; }
</style>
