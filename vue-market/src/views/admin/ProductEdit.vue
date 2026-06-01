<template>
  <AdminLayout>
    <h2>{{ isNew ? '添加商品' : '编辑商品' }}</h2>
    <el-form :model="form" label-width="100px" style="max-width:600px;margin-top:20px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" /></el-form-item>
      <el-form-item label="库存"><el-input-number v-model="form.stock" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.detail" type="textarea" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:100%">
          <el-option v-for="c in categories" :key="c.id" :label="c.name + ' (ID:' + c.id + ')'" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="主图URL"><el-input v-model="form.mainImage" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getAdminProduct, createAdminProduct, updateAdminProduct, getCategories } from '../../api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === '0')
const categories = ref([])
const form = ref({ name: '', price: 0, stock: 0, detail: '', categoryId: null, mainImage: '', status: 1, sales: 0 })

onMounted(async () => {
  const [{ data: catData }, productData] = await Promise.all([
    getCategories(),
    !isNew.value ? getAdminProduct(route.params.id) : Promise.resolve(null)
  ])
  if (catData.code === 200) categories.value = catData.data || []
  if (productData && productData.data.code === 200) form.value = { ...form.value, ...productData.data.data }
})

const save = async () => {
  const { data } = isNew.value
    ? await createAdminProduct(form.value)
    : await updateAdminProduct(route.params.id, form.value)
  if (data.code === 200) { ElMessage.success('保存成功'); router.push('/admin/products') }
  else ElMessage.error(data.msg || '保存失败')
}
</script>
