<template>
  <AdminLayout>
    <div class="admin-seckill">
      <div class="toolbar">
        <h2>秒杀管理</h2>
        <el-button type="primary" @click="addNew">添加秒杀商品</el-button>
      </div>
      <el-table :data="products" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名" />
        <el-table-column prop="seckillPrice" label="秒杀价">
          <template #default="{ row }">&yen;{{ (row.seckillPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="del(row.id)">
              <template #reference><el-button size="small" type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="dialogVisible" title="秒杀商品" width="500px">
        <el-form :model="form" label-width="80px">
          <el-form-item label="商品ID"><el-input-number v-model="form.productId" /></el-form-item>
          <el-form-item label="秒杀价"><el-input-number v-model="form.seckillPrice" :precision="2" /></el-form-item>
          <el-form-item label="库存"><el-input-number v-model="form.stock" /></el-form-item>
          <el-form-item label="开始时间"><el-input v-model="form.startTime" /></el-form-item>
          <el-form-item label="结束时间"><el-input v-model="form.endTime" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminSeckillProducts, createAdminSeckillProduct, updateAdminSeckillProduct, deleteAdminSeckillProduct } from '../../api/admin'
import { ElMessage } from 'element-plus'

const products = ref([])
const dialogVisible = ref(false)
const form = ref({})
const editingId = ref(null)

const fetch = async () => {
  const { data } = await getAdminSeckillProducts({ pageNum: 1, pageSize: 50 })
  if (data.code === 200) products.value = data.data || []
}

const addNew = () => { form.value = { productId: 0, seckillPrice: 0, stock: 0, startTime: '', endTime: '' }; editingId.value = null; dialogVisible.value = true }
const edit = (row) => { form.value = { ...row }; editingId.value = row.id; dialogVisible.value = true }
const save = async () => {
  const { data } = editingId.value
    ? await updateAdminSeckillProduct(editingId.value, form.value)
    : await createAdminSeckillProduct(form.value)
  if (data.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; fetch() }
  else ElMessage.error(data.msg || '保存失败')
}
const del = async (id) => { await deleteAdminSeckillProduct(id); fetch() }

onMounted(fetch)
</script>

<style scoped>
.admin-seckill { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
