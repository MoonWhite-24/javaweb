<template>
  <DefaultLayout>
    <div class="home">
      <el-carousel height="380px" :interval="5000" arrow="always">
        <el-carousel-item v-for="(b, i) in banners" :key="i">
          <div class="banner" :style="{ background: b.bg }">
            <div class="banner-text">
              <h2>{{ b.title }}</h2>
              <p>{{ b.subtitle }}</p>
              <el-button round class="banner-btn" @click.stop="$router.push(b.link)">{{ b.btnText }}</el-button>
            </div>
            <div class="banner-cards">
              <div class="preview-card" v-for="p in b.products.slice(0,3)" :key="p.id" @click.stop="$router.push('/products/' + p.id)">
                <img :src="productImage(p.mainImage)" class="preview-img" @error="usePlaceholderImage" />
                <span class="preview-name">{{ p.name }}</span>
                <span class="preview-price">&yen;{{ (p.price || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <section class="seckill-section">
        <div class="section-header"><h2>限时秒杀</h2><el-button text type="primary" @click="$router.push('/seckill')">查看更多 →</el-button></div>
        <el-row :gutter="20">
          <el-col v-for="p in seckillProducts.slice(0,4)" :key="p.id" :sm="12" :md="6">
            <ProductCard :product="p" />
          </el-col>
        </el-row>
        <div v-if="!seckillProducts.length" class="empty">暂无秒杀活动</div>
      </section>
      <section class="featured-section">
        <div class="section-header"><h2>热门推荐</h2><el-button text type="primary" @click="$router.push('/products')">查看更多 →</el-button></div>
        <el-row :gutter="20">
          <el-col v-for="p in hotProducts" :key="p.id" :sm="12" :md="6">
            <ProductCard :product="p" />
          </el-col>
        </el-row>
      </section>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import ProductCard from '../components/ProductCard.vue'
import { getSeckillProducts } from '../api/seckill'
import { getProducts } from '../api/product'
import { productImage, usePlaceholderImage } from '../utils/image'

const banners = reactive([
  { title: '新品首发', subtitle: '最新好物限时特惠', btnText: '立即抢新', link: '/products', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', products: [] },
  { title: '手机狂欢', subtitle: '旗舰机型最高减1000元', btnText: '去抢购', link: '/products?categoryId=14', bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', products: [] },
  { title: '生鲜直达', subtitle: '新鲜食材极速到家', btnText: '立即选购', link: '/products?categoryId=13', bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', products: [] },
  { title: '数码配件', subtitle: '品质配件全场8折', btnText: '去逛逛', link: '/products?categoryId=7', bg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', products: [] },
])

const seckillProducts = ref([])
const hotProducts = ref([])

onMounted(async () => {
  const categoryIds = [null, 14, 13, 7]
  const fetches = categoryIds.map(cid => {
    const params = { orderBy: 'sales_desc', pageSize: 3 }
    if (cid) params.categoryId = cid
    return getProducts(params)
  })

  const [seckillRes, hotRes, allProductsRes, ...bannerRess] = await Promise.all([
    getSeckillProducts(),
    getProducts({ orderBy: 'sales_desc', pageSize: 8 }),
    getProducts({ pageSize: 200 }),
    ...fetches
  ])

  const productMap = {}
  const allList = allProductsRes.data.code === 200 ? (allProductsRes.data.data.list || allProductsRes.data.data.records || []) : []
  allList.forEach(p => { productMap[p.id] = p })

  if (seckillRes.data.code === 200) {
    seckillProducts.value = (seckillRes.data.data || []).map(s => ({
      ...s,
      name: (productMap[s.productId] || {}).name || `商品#${s.productId}`,
      price: s.seckillPrice,
      mainImage: (productMap[s.productId] || {}).mainImage || '',
      sales: (productMap[s.productId] || {}).sales || 0
    }))
  }
  if (hotRes.data.code === 200) hotProducts.value = hotRes.data.data.list || hotRes.data.data.records || []
  bannerRess.forEach((res, i) => {
    if (res.data.code === 200) banners[i].products = res.data.data.list || res.data.data.records || []
  })
})
</script>

<style scoped>
.banner { display: flex; justify-content: space-between; align-items: center; height: 100%; padding: 0 8%; overflow: hidden; }
.banner-text { color: #fff; flex-shrink: 0; z-index: 1; }
.banner-text h2 { font-size: 34px; margin-bottom: 8px; text-shadow: 0 2px 8px rgba(0,0,0,0.25); }
.banner-text p { font-size: 16px; opacity: 0.85; margin-bottom: 20px; }
.banner-btn { --el-button-bg-color: rgba(255,255,255,0.15); --el-button-border-color: rgba(255,255,255,0.3); --el-button-text-color: #fff; --el-button-hover-bg-color: rgba(255,255,255,0.3); --el-button-hover-border-color: rgba(255,255,255,0.5); }

.banner-cards { flex: 1; display: flex; align-items: center; justify-content: center; gap: 24px; min-width: 0; }
.preview-card { display: flex; flex-direction: column; align-items: center; width: 140px; height: 190px; border-radius: 12px; overflow: hidden; background: rgba(255,255,255,0.15); backdrop-filter: blur(6px); transition: transform 0.2s; cursor: pointer; }
.preview-card:hover { transform: translateY(-4px); }
.preview-img { width: 100%; height: 120px; object-fit: cover; display: block; }
.preview-name { font-size: 13px; color: #fff; margin-top: 6px; padding: 0 8px; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%; }
.preview-price { font-size: 14px; color: #ffe0e0; font-weight: 600; margin-top: 2px; }

.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h2 { margin: 0; }
.seckill-section, .featured-section { padding: 40px; max-width: 1200px; margin: 0 auto; }
.empty { text-align: center; padding: 40px; color: #909399; }
</style>
