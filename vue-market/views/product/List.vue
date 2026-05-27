<template>
  <DefaultLayout>
    <div class="product-page">
      <aside class="sidebar-col"><Sidebar :categories="categories" /></aside>
      <main class="main-col">
        <div class="toolbar">
          <el-radio-group v-model="orderBy" @change="fetch">
            <el-radio-button value="default">默认</el-radio-button>
            <el-radio-button value="price_asc">价格↑</el-radio-button>
            <el-radio-button value="price_desc">价格↓</el-radio-button>
            <el-radio-button value="sales_desc">销量最佳</el-radio-button>
          </el-radio-group>
        </div>
        <el-row :gutter="16">
          <el-col v-for="p in products" :key="p.id" :sm="12" :md="8" :lg="6">
            <ProductCard :product="p" />
          </el-col>
        </el-row>
        <div v-if="!products.length" class="empty">暂无商品</div>
        <Pagination :total="total" :page="pageNum" @change="handlePage" />
      </main>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import Sidebar from '../../components/Sidebar.vue'
import ProductCard from '../../components/ProductCard.vue'
import Pagination from '../../components/Pagination.vue'
import { getProducts, getCategories } from '../../api/product'

const route = useRoute()
const products = ref([])
const categories = ref([])
const total = ref(0)
const pageNum = ref(1)
const orderBy = ref('default')

const fetch = async () => {
  const params = { pageNum: pageNum.value, pageSize: 20 }
  if (orderBy.value !== 'default') params.orderBy = orderBy.value
  if (route.query.categoryId) params.categoryId = route.query.categoryId
  const { data } = await getProducts(params)
  if (data.code === 200) {
    products.value = data.data.list || data.data.records || []
    total.value = data.data.total || 0
  }
}

const handlePage = (p) => { pageNum.value = p; fetch() }

watch(() => route.query.categoryId, () => { pageNum.value = 1; fetch() })

onMounted(async () => {
  const { data } = await getCategories()
  if (data.code === 200) categories.value = data.data || []
  fetch()
})
</script>

<style scoped>
.product-page { display: flex; max-width: 1200px; margin: 0 auto; padding: 20px; }
.sidebar-col { width: 220px; flex-shrink: 0; }
.main-col { flex: 1; margin-left: 20px; }
.toolbar { margin-bottom: 16px; }
.empty { text-align: center; padding: 60px; color: #909399; }
</style>
