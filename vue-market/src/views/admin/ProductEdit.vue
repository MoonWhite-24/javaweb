<template>
  <AdminLayout>
    <h2>{{ isNew ? '添加商品' : '编辑商品' }}</h2>
    <el-form :model="form" label-width="100px" style="max-width:700px;margin-top:20px">
      <el-form-item label="名称">
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="副标题">
        <el-input v-model="form.subtitle" placeholder="请输入副标题" />
      </el-form-item>
      <el-form-item label="价格">
        <el-input-number v-model="form.price" :precision="2" :min="0" />
      </el-form-item>
      <el-form-item label="原价">
        <el-input-number v-model="form.originalPrice" :precision="2" :min="0" />
      </el-form-item>
      <el-form-item label="库存">
        <el-input-number v-model="form.stock" :min="0" />
      </el-form-item>
      <el-form-item label="分类">
        <div style="display:flex;gap:10px;width:100%">
          <el-cascader
            v-model="selectedCategoryPath"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true }"
            placeholder="请选择分类"
            clearable
            style="flex:1"
            @change="onCategoryChange"
          />
          <el-button type="success" @click="showAddCategory = true">
            <el-icon><Plus /></el-icon> 新增分类
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="主图URL">
        <el-input v-model="form.mainImage" placeholder="请输入主图URL" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="请输入商品描述" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">上架</el-radio>
          <el-radio :value="2">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>

    <!-- 新增分类对话框 -->
    <el-dialog v-model="showAddCategory" title="新增分类" width="450">
      <el-form :model="newCategory" label-width="80px">
        <el-form-item label="父级分类">
          <el-cascader
            v-model="newCategory.parentPath"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true }"
            placeholder="留空则为顶级分类"
            clearable
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="newCategory.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="newCategory.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCategory = false">取消</el-button>
        <el-button type="primary" @click="addCategory">确定</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../../layouts/AdminLayout.vue'
import {
  getAdminProduct, createAdminProduct, updateAdminProduct,
  getAdminCategoryTree, createAdminCategory
} from '../../api/admin'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === '0')

const categoryTree = ref([])
const selectedCategoryPath = ref([])
const showAddCategory = ref(false)
const newCategory = ref({ name: '', parentPath: [], sortOrder: 0 })

const form = ref({
  name: '', subtitle: '', price: 0, originalPrice: 0, stock: 0,
  detail: '', categoryId: null, mainImage: '', status: 1, sales: 0
})

// Build tree from flat list
const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => { map[item.id] = { ...item, children: [] } })
  list.forEach(item => {
    if (item.parentId === 0 || !map[item.parentId]) {
      roots.push(map[item.id])
    } else {
      map[item.parentId].children.push(map[item.id])
    }
  })
  // Remove empty children arrays for leaf nodes
  const clean = (nodes) => nodes.map(n => {
    if (n.children.length === 0) delete n.children
    else clean(n.children)
    return n
  })
  return clean(roots)
}

// Find category path from tree
const findPath = (tree, targetId, path = []) => {
  for (const node of tree) {
    const currentPath = [...path, node.id]
    if (node.id === targetId) return currentPath
    if (node.children) {
      const found = findPath(node.children, targetId, currentPath)
      if (found) return found
    }
  }
  return null
}

const onCategoryChange = (val) => {
  form.value.categoryId = val && val.length > 0 ? val[val.length - 1] : null
}

onMounted(async () => {
  const { data: catData } = await getAdminCategoryTree()
  if (catData.code === 200) {
    const flat = catData.data || []
    categoryTree.value = buildTree(flat)
  }

  if (!isNew.value) {
    const { data: productData } = await getAdminProduct(route.params.id)
    if (productData.code === 200) {
      form.value = { ...form.value, ...productData.data }
      // Set cascader path
      if (form.value.categoryId) {
        selectedCategoryPath.value = findPath(categoryTree.value, form.value.categoryId) || []
      }
    }
  }
})

const save = async () => {
  if (!form.value.name) { ElMessage.warning('请输入商品名称'); return }
  if (!form.value.categoryId) { ElMessage.warning('请选择分类'); return }
  const { data } = isNew.value
    ? await createAdminProduct(form.value)
    : await updateAdminProduct(route.params.id, form.value)
  if (data.code === 200) { ElMessage.success('保存成功'); router.push('/admin/products') }
  else ElMessage.error(data.msg || '保存失败')
}

const addCategory = async () => {
  if (!newCategory.value.name) { ElMessage.warning('请输入分类名称'); return }
  const parentPath = newCategory.value.parentPath
  const parentId = parentPath && parentPath.length > 0 ? parentPath[parentPath.length - 1] : 0
  const level = parentId === 0 ? 1 : (parentPath ? parentPath.length + 1 : 1)

  const { data } = await createAdminCategory({
    name: newCategory.value.name,
    parentId,
    level,
    sortOrder: newCategory.value.sortOrder
  })

  if (data.code === 200) {
    ElMessage.success('分类添加成功')
    showAddCategory.value = false
    newCategory.value = { name: '', parentPath: [], sortOrder: 0 }
    // Reload category tree
    const { data: catData } = await getAdminCategoryTree()
    if (catData.code === 200) {
      categoryTree.value = buildTree(catData.data || [])
    }
  } else {
    ElMessage.error(data.msg || '添加失败')
  }
}
</script>
