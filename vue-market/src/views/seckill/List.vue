<template>
  <DefaultLayout>
    <div class="seckill-page">
      <h2>限时秒杀</h2>
      <el-row :gutter="20">
        <el-col v-for="p in products" :key="p.id" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <img :src="productImage(p.mainImage)" class="seckill-img" @error="usePlaceholderImage" />
            <h3>{{ p.name }}</h3>
            <p class="seckill-price">&yen;{{ (p.seckillPrice || 0).toFixed(2) }}</p>
            <p class="original-price" v-if="p.originalPrice">&yen;{{ (p.originalPrice || 0).toFixed(2) }}</p>
            <p class="stock-info">剩余 {{ p.stockCount || 0 }} 件</p>
            <el-button type="danger" @click="doSeckill(p)" :disabled="!p.stockCount">
              {{ p.stockCount > 0 ? '立即秒杀' : '已抢光' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>
      <div v-if="!products.length" class="empty">暂无秒杀活动</div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { getSeckillProducts, executeSeckill } from '../../api/seckill'
import { getProducts } from '../../api/product'
import { ElMessage } from 'element-plus'
import { productImage, usePlaceholderImage } from '../../utils/image'

const router = useRouter()
const products = ref([])

onMounted(async () => {
  const [seckillRes, allRes] = await Promise.all([
    getSeckillProducts(),
    getProducts({ pageSize: 200 })
  ])
  const productMap = {}
  const allList = allRes.data.code === 200 ? (allRes.data.data.list || allRes.data.data.records || []) : []
  allList.forEach(p => { productMap[p.id] = p })

  if (seckillRes.data.code === 200) {
    products.value = (seckillRes.data.data || []).map(s => {
      const prod = productMap[s.productId] || {}
      return {
        ...s,
        name: prod.name || `商品#${s.productId}`,
        mainImage: prod.mainImage || '',
        originalPrice: prod.price || null
      }
    })
  }
})

const doSeckill = async (p) => {
  const { data } = await executeSeckill(p.id)
  if (data.code === 200 && data.data && data.data.success) {
    router.push(`/orders/pay/${data.data.orderNo}`)
  } else {
    ElMessage.error(data.data?.message || data.msg || '秒杀失败')
  }
}
</script>

<style scoped>
.seckill-page { max-width: 1200px; margin: 40px auto; padding: 20px; }
.seckill-page h2 { margin-bottom: 20px; }
.seckill-img { width: 100%; height: 180px; object-fit: cover; border-radius: 8px; margin-bottom: 12px; }
h3 { font-size: 15px; margin-bottom: 8px; }
.seckill-price { font-size: 22px; font-weight: bold; color: #f56c6c; }
.original-price { text-decoration: line-through; color: #909399; margin: 4px 0; }
.stock-info { font-size: 13px; color: #909399; margin: 8px 0; }
.empty { text-align: center; padding: 60px; color: #909399; }
</style>
