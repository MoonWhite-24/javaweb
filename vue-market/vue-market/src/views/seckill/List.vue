<template>
  <DefaultLayout>
    <div class="seckill-page">
      <h2>限时秒杀</h2>
      <el-row :gutter="20">
        <el-col v-for="p in products" :key="p.id" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <h3>{{ p.productName || p.name }}</h3>
            <p class="seckill-price">&yen;{{ (p.seckillPrice || p.price || 0).toFixed(2) }}</p>
            <p class="original-price">&yen;{{ (p.price || 0).toFixed(2) }}</p>
            <el-button type="danger" @click="doSeckill(p.id)" :disabled="p.stock <= 0">
              {{ p.stock > 0 ? '立即秒杀' : '已抢光' }}
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
import { ElMessage } from 'element-plus'

const router = useRouter()
const products = ref([])

onMounted(async () => {
  const { data } = await getSeckillProducts()
  if (data.code === 200) products.value = data.data || []
})

const doSeckill = async (productId) => {
  const { data } = await executeSeckill(productId)
  if (data.code === 200) {
    router.push(`/seckill/result/${productId}`)
  } else {
    ElMessage.error(data.msg || '秒杀失败')
  }
}
</script>

<style scoped>
.seckill-page { max-width: 1200px; margin: 40px auto; padding: 20px; }
.seckill-page h2 { margin-bottom: 20px; }
.seckill-price { font-size: 22px; font-weight: bold; color: #f56c6c; }
.original-price { text-decoration: line-through; color: #909399; margin: 8px 0; }
.empty { text-align: center; padding: 60px; color: #909399; }
</style>
