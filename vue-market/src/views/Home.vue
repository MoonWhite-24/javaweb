<template>
  <DefaultLayout>
    <div class="home">
      <section class="hero">
        <h1>JavaWeb Market 2.0</h1>
        <p>全新的购物体验</p>
        <el-button type="primary" size="large" @click="$router.push('/products')">立即选购</el-button>
      </section>
      <section class="seckill-section">
        <h2>限时秒杀</h2>
        <el-row :gutter="20">
          <el-col v-for="p in seckillProducts.slice(0,4)" :key="p.id" :sm="12" :md="6">
            <ProductCard :product="p" />
          </el-col>
        </el-row>
      </section>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import ProductCard from '../components/ProductCard.vue'
import { getSeckillProducts } from '../api/seckill'

const seckillProducts = ref([])
onMounted(async () => {
  const { data } = await getSeckillProducts()
  if (data.code === 200) seckillProducts.value = data.data || []
})
</script>

<style scoped>
.hero { text-align: center; padding: 80px 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
.hero h1 { font-size: 36px; margin-bottom: 12px; }
.hero p { font-size: 18px; margin-bottom: 24px; opacity: 0.9; }
.seckill-section { padding: 40px; max-width: 1200px; margin: 0 auto; }
.seckill-section h2 { margin-bottom: 20px; }
</style>
