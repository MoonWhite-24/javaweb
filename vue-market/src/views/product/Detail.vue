<template>
  <DefaultLayout>
    <div v-if="product" class="product-detail">
      <el-row :gutter="40">
        <el-col :sm="24" :md="10">
          <img :src="productImage(product.mainImage)" class="detail-img" @error="usePlaceholderImage" />
        </el-col>
        <el-col :sm="24" :md="14">
          <h1>{{ product.name }}</h1>
          <p class="detail-price">&yen;{{ (product.price || 0).toFixed(2) }}</p>
          <p class="detail-meta">库存: {{ product.stock || 0 }} | 销量: {{ product.sales || 0 }}</p>
          <div class="desc" v-html="product.detail"></div>
          <el-input-number v-model="qty" :min="1" :max="product.stock || 99" class="qty-input" />
          <el-button type="primary" size="large" @click="addToCart" :disabled="!product.stock">加入购物车</el-button>
        </el-col>
      </el-row>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { getProductDetail } from '../../api/product'
import { useCartStore } from '../../stores/cart'
import { ElMessage } from 'element-plus'
import { productImage, usePlaceholderImage } from '../../utils/image'

const route = useRoute()
const cartStore = useCartStore()
const product = ref(null)
const qty = ref(1)

onMounted(async () => {
  const { data } = await getProductDetail(route.params.id)
  if (data.code === 200) product.value = data.data
})

const addToCart = async () => {
  await cartStore.add(product.value.id, qty.value)
  ElMessage.success('已加入购物车')
}
</script>

<style scoped>
.product-detail { max-width: 1000px; margin: 40px auto; padding: 20px; background: #fff; border-radius: 8px; }
.detail-img { width: 100%; border-radius: 8px; }
.detail-price { font-size: 28px; color: #f56c6c; font-weight: bold; margin: 16px 0; }
.detail-meta { color: #909399; margin-bottom: 12px; }
.desc { color: #606266; line-height: 1.8; margin: 16px 0; }
.qty-input { margin-right: 16px; }
</style>
