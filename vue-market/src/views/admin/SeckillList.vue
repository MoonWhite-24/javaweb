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
        <el-table-column prop="stockCount" label="库存" width="80" />
        <el-table-column label="开始时间">
          <template #default="{ row }">{{ row.startTime?.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="结束时间">
          <template #default="{ row }">{{ row.endTime?.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="del(row.id)">
              <template #reference><el-button size="small" type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑秒杀商品' : '添加秒杀商品'" width="500px">
        <el-form :model="form" label-width="80px">
          <el-form-item label="选择商品">
            <el-select v-model="form.productId" filterable placeholder="搜索商品名或ID" style="width:100%">
              <el-option v-for="p in allProducts" :key="p.id" :label="`[${p.id}] ${p.name}`" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="秒杀价"><el-input-number v-model="form.seckillPrice" :precision="2" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="库存"><el-input-number v-model="form.stockCount" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
          </el-form-item>
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
import { getProducts } from '../../api/product'
import { ElMessage } from 'element-plus'

const products = ref([])
const allProducts = ref([])
const dialogVisible = ref(false)
const form = ref({})
const editingId = ref(null)

const resolveNames = (list) => {
  const map = {}
  allProducts.value.forEach(p => { map[p.id] = p.name })
  list.forEach(s => { s.productName = map[s.productId] || `商品#${s.productId}` })
  return list
}
const fetch = async () => {
  const { data } = await getAdminSeckillProducts({ pageNum: 1, pageSize: 50 })
  if (data.code === 200) products.value = resolveNames(data.data || [])
}

const fetchAllProducts = async () => {
  const { data } = await getProducts({ pageSize: 200 })
  if (data.code === 200) allProducts.value = data.data.list || data.data.records || []
}

const fmtTime = (d) => { const pad = n => String(n).padStart(2, '0'); return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}` }
const addNew = async () => {
  if (!allProducts.value.length) await fetchAllProducts()
  const now = new Date()
  const later = new Date(now.getTime() + 3600000)
  form.value = { productId: null, seckillPrice: 0, stockCount: 100, startTime: fmtTime(now), endTime: fmtTime(later) }
  editingId.value = null
  dialogVisible.value = true
}

const edit = async (row) => {
  if (!allProducts.value.length) await fetchAllProducts()
  form.value = { ...row }
  editingId.value = row.id
  dialogVisible.value = true
}

const save = async () => {
  if (!form.value.productId) { ElMessage.warning('请选择商品'); return }
  const payload = { ...form.value }
  const { data } = editingId.value
    ? await updateAdminSeckillProduct(editingId.value, payload)
    : await createAdminSeckillProduct(payload)
  if (data.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; fetch() }
  else ElMessage.error(data.msg || '保存失败')
}

const del = async (id) => { await deleteAdminSeckillProduct(id); fetch() }

onMounted(() => { fetch(); fetchAllProducts() })
</script>

<style scoped>
.admin-seckill { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
