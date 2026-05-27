<template>
  <DefaultLayout>
    <div class="seckill-result">
      <el-result icon="success" title="秒杀结果">
        <template #sub-title>秒杀ID: {{ result }}</template>
        <template #extra>
          <el-button type="primary" @click="$router.push('/seckill')">返回秒杀</el-button>
          <el-button @click="$router.push('/orders')">查看订单</el-button>
        </template>
      </el-result>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '../../layouts/DefaultLayout.vue'
import { getSeckillResult } from '../../api/seckill'

const route = useRoute()
const result = ref(null)

onMounted(async () => {
  const { data } = await getSeckillResult(route.params.id)
  if (data.code === 200) result.value = data.data
})
</script>

<style scoped>
.seckill-result { max-width: 600px; margin: 60px auto; }
</style>
