<template>
  <AdminLayout>
    <div class="admin-orders">
      <h2>订单管理</h2>
      <el-table :data="orders" border style="margin-top:20px">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productNames" label="商品" min-width="200">
          <template #default="{ row }">{{ row.productNames || '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="金额">
          <template #default="{ row }">&yen;{{ (row.totalPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','','','','danger'][row.status] || 'warning'">{{ ['待支付','已支付','已发货','已收货','已完成','已取消'][row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/admin/orders/${row.orderNo}`)">详情</el-button>
            <el-popconfirm title="确定删除该订单?" @confirm="del(row.orderNo)">
              <template #reference><el-button size="small" type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:20px;display:flex;justify-content:space-between;align-items:center">
        <el-button type="warning" @click="refreshAllImages" :loading="refreshing">
          批量刷新所有订单图片
        </el-button>
        <el-pagination
          v-if="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetch"
          @size-change="fetch"
        />
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminOrders, deleteAdminOrder, refreshAllOrderImages } from '../../api/admin'
import { ElMessage } from 'element-plus'

const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const refreshing = ref(false)

const fetch = async () => {
  const { data } = await getAdminOrders({ pageNum: pageNum.value, pageSize: pageSize.value })
  if (data.code === 200) {
    orders.value = data.data.list || data.data.records || []
    total.value = data.data.total || 0
  }
}

const del = async (orderNo) => {
  const { data } = await deleteAdminOrder(orderNo)
  if (data.code === 200) {
    ElMessage.success('删除成功')
    fetch()
  } else {
    ElMessage.error(data.msg || '删除失败')
  }
}

const refreshAllImages = async () => {
  refreshing.value = true
  try {
    const { data } = await refreshAllOrderImages()
    if (data.code === 200) {
      const totalUpdated = data.data || 0
      if (totalUpdated > 0) {
        ElMessage.success(`已更新 ${totalUpdated} 个商品图片`)
        fetch() // 刷新列表
      } else {
        ElMessage.info('所有图片已是最新')
      }
    } else {
      ElMessage.error(data.msg || '刷新失败')
    }
  } catch (e) {
    ElMessage.error('刷新失败: ' + (e.message || '未知错误'))
  } finally {
    refreshing.value = false
  }
}

onMounted(() => fetch())
</script>

<style scoped>
.admin-orders { padding: 20px; }
</style>
