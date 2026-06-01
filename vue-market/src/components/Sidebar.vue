<template>
  <div class="sidebar">
    <h3>商品分类</h3>
    <ul class="category-tree">
      <template v-for="cat in categories" :key="cat.id">
        <li>
          <a v-if="!cat.children?.length"
            :href="`/products?categoryId=${cat.id}`"
            @click.prevent="$router.push(`/products?categoryId=${cat.id}`)">
            {{ cat.name }}
          </a>
          <div v-else>
            <a class="parent"
              :href="`/products?categoryId=${cat.id}`"
              @click.prevent="$router.push(`/products?categoryId=${cat.id}`)">
              {{ cat.name }}
            </a>
            <ul>
              <template v-for="sub in cat.children" :key="sub.id">
                <li>
                  <a v-if="!sub.children?.length"
                    :href="`/products?categoryId=${sub.id}`"
                    @click.prevent="$router.push(`/products?categoryId=${sub.id}`)">
                    {{ sub.name }}
                  </a>
                  <div v-else>
                    <a class="parent"
                      :href="`/products?categoryId=${sub.id}`"
                      @click.prevent="$router.push(`/products?categoryId=${sub.id}`)">
                      {{ sub.name }}
                    </a>
                    <ul>
                      <li v-for="sub2 in sub.children" :key="sub2.id">
                        <a :href="`/products?categoryId=${sub2.id}`"
                          @click.prevent="$router.push(`/products?categoryId=${sub2.id}`)">
                          {{ sub2.name }}
                        </a>
                      </li>
                    </ul>
                  </div>
                </li>
              </template>
            </ul>
          </div>
        </li>
      </template>
    </ul>
  </div>
</template>

<script setup>
defineProps({ categories: { type: Array, default: () => [] } })
</script>

<style scoped>
.sidebar { background: #fff; border-radius: 8px; padding: 16px; }
h3 { margin-bottom: 12px; font-size: 16px; }
ul { list-style: none; padding: 0; margin: 0; }
.category-tree li { margin: 2px 0; }
.category-tree a {
  display: block; padding: 6px 12px; color: #303133; text-decoration: none;
  border-radius: 4px; font-size: 14px; cursor: pointer;
}
.category-tree a:hover, .category-tree a.router-link-active { color: #409eff; background: #ecf5ff; }
.category-tree a.parent { font-weight: 600; }
.category-tree ul { padding-left: 16px; }
</style>
