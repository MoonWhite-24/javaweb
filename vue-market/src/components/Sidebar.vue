<template>
  <div class="sidebar">
    <h3>商品分类</h3>
    <el-menu>
      <template v-for="cat in categories" :key="cat.id">
        <el-menu-item v-if="!cat.children?.length" :index="`/products?categoryId=${cat.id}`">
          {{ cat.name }}
        </el-menu-item>
        <el-sub-menu v-else :index="`cat-${cat.id}`">
          <template #title>{{ cat.name }}</template>
          <template v-for="sub in cat.children" :key="sub.id">
            <el-menu-item v-if="!sub.children?.length" :index="`/products?categoryId=${sub.id}`">
              {{ sub.name }}
            </el-menu-item>
            <el-sub-menu v-else :index="`sub-${sub.id}`">
              <template #title>{{ sub.name }}</template>
              <el-menu-item v-for="sub2 in sub.children" :key="sub2.id" :index="`/products?categoryId=${sub2.id}`">
                {{ sub2.name }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-sub-menu>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
defineProps({ categories: { type: Array, default: () => [] } })
</script>

<style scoped>
.sidebar { background: #fff; border-radius: 8px; padding: 16px; }
h3 { margin-bottom: 12px; font-size: 16px; }
</style>
