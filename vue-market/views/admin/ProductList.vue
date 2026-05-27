<template>
  <AdminLayout>
    <div class="admin-products">
      <div class="toolbar">
        <h2>商品管理</h2>
        <el-button type="primary" @click="$router.push('/admin/products/0/edit')">添加商品</el-button>
      </div>
      <el-table :data="products" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="price" label="价格">
          <template #default="{ row }">&yen;{{ (row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/admin/products/${row.id}/edit`)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination :total="total" :page="pageNum" @change="handlePage" />
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import Pagination from '../../components/Pagination.vue'
import { getAdminProducts, updateProductStatus } from '../../api/admin'
import { ElMessage } from 'element-plus'

const products = ref([])
const total = ref(0)
const pageNum = ref(1)

const fetch = async () => {
  const { data } = await getAdminProducts({ pageNum: pageNum.value })
  if (data.code === 200) { products.value = data.data.list || data.data.records || []; total.value = data.data.total || 0 }
}

const handlePage = (p) => { pageNum.value = p; fetch() }
const toggleStatus = async (row) => {
  await updateProductStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.admin-products { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
