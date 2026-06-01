<template>
  <DefaultLayout>
    <div class="order-page">
      <h2>我的订单</h2>
      <el-table v-if="orders.length" :data="orders" style="width:100%">
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div v-if="row.items && row.items.length" class="order-items">
              <span v-for="item in row.items" :key="item.id" class="item-name">
                {{ item.productName }}
              </span>
            </div>
            <span v-else class="no-items">-</span>
          </template>
        </el-table-column>
        <el-table-column label="金额">
          <template #default="{ row }">&yen;{{ (row.totalPrice || row.payment || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/orders/${row.orderNo}`)">详情</el-button>
            <el-button v-if="row.status === 0" size="small" type="primary" @click="$router.push(`/orders/pay/${row.orderNo}`)">支付</el-button>
            <el-button v-if="row.status === 0" size="small" type="danger" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="row.status === 1 || row.status === 4 || row.status === 5" size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        style="margin-top:20px;justify-content:center"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetch"
      />
      <div v-else class="empty">暂无订单</div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { getOrders, cancelOrder, deleteOrder } from '../../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已收货', 4: '已完成', 5: '已取消' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: '', 3: '', 4: 'info', 5: 'danger' }

const statusText = (s) => statusMap[s] ?? `未知(${s})`
const statusType = (s) => statusTypeMap[s] ?? 'info'

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定取消订单 #${row.orderNo}？`, '确认取消', { type: 'warning' })
    const { data } = await cancelOrder(row.orderNo)
    if (data.code === 200) {
      ElMessage.success('订单已取消')
      row.status = 5
    } else {
      ElMessage.error(data.msg || '取消失败')
    }
  } catch { /* user cancelled dialog */ }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除订单 #${row.orderNo}？此操作不可恢复。`, '确认删除', { type: 'warning' })
    const { data } = await deleteOrder(row.orderNo)
    if (data.code === 200) {
      ElMessage.success('订单已删除')
      orders.value = orders.value.filter(o => o.orderNo !== row.orderNo)
    } else {
      ElMessage.error(data.msg || '删除失败')
    }
  } catch { /* user cancelled dialog */ }
}

const fetch = async () => {
  const { data } = await getOrders({ pageNum: pageNum.value, pageSize: pageSize.value })
  if (data.code === 200) {
    orders.value = data.data.list || data.data.records || []
    total.value = data.data.total || 0
  }
}

onMounted(() => fetch())
</script>

<style scoped>
.order-page { max-width: 1100px; margin: 40px auto; padding: 20px; }
.order-page h2 { margin-bottom: 20px; }
.order-items { display: flex; flex-wrap: wrap; gap: 4px; }
.item-name { font-size: 13px; background: #f0f2f5; padding: 2px 8px; border-radius: 4px; }
.no-items { color: #c0c4cc; }
.empty { text-align: center; padding: 60px; color: #909399; }
</style>
