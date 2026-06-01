<template>
  <el-card :body-style="{ padding: '0' }" shadow="hover" class="product-card" @click="goDetail">
    <img :src="product.mainImage || 'https://placehold.co/300x200?text=No+Image'" class="product-img" />
    <div class="product-info">
      <h3>{{ product.name }}</h3>
      <p class="price">&yen;{{ (product.price || 0).toFixed(2) }}</p>
      <p class="sales">已售 {{ product.sales || 0 }}</p>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({ product: Object })
const router = useRouter()
const goDetail = () => router.push(`/products/${props.product.id}`)
</script>

<style scoped>
.product-card { cursor: pointer; }
.product-img { width: 100%; height: 200px; object-fit: cover; }
.product-info { padding: 14px; }
.product-info h3 { font-size: 14px; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.sales { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
