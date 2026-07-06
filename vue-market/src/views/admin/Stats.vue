<template>
  <AdminLayout>
    <div class="admin-stats">
      <div class="stats-header">
        <h2>数据统计</h2>
        <el-radio-group v-model="days" @change="loadTrendData">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="14">近 14 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 概览卡片 -->
      <el-row :gutter="20" class="stat-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="今日订单数" :value="stats.todayOrderCount || 0">
              <template #prefix><el-icon style="color: #409EFF"><Document /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="今日收入" :value="Number(stats.todayRevenue || 0)" :precision="2" prefix="¥">
              <template #prefix><el-icon style="color: #67C23A"><Money /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="今日新增用户" :value="stats.todayNewUsers || 0">
              <template #prefix><el-icon style="color: #9B59B6"><UserFilled /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="总用户数" :value="stats.totalUsers || 0">
              <template #prefix><el-icon style="color: #E6A23C"><User /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- 趋势图表 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>订单量趋势（近 {{ days }} 天）</span>
            </template>
            <div ref="orderTrendRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>收入趋势（近 {{ days }} 天）</span>
            </template>
            <div ref="revenueTrendRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>订单状态分布</span>
            </template>
            <div ref="statusDistRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>热销商品 TOP 10</span>
            </template>
            <div ref="topProductsRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { getStats, getStatsTrend, getStatsOrderStatus } from '../../api/admin'
import { Document, Money, UserFilled, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const days = ref(7)
const stats = ref({})

const orderTrendRef = ref(null)
const revenueTrendRef = ref(null)
const statusDistRef = ref(null)
const topProductsRef = ref(null)

let orderTrendChart = null
let revenueTrendChart = null
let statusDistChart = null
let topProductsChart = null

const initLineChart = (el, data, name, color, isMoney = false) => {
  if (!el) return null
  const chart = echarts.init(el)
  const dates = data.map(d => d.date || d.DATE || '')
  const values = data.map(d => {
    const key = Object.keys(d).find(k => k !== 'date' && k !== 'DATE')
    return Number(d[key] || 0)
  })
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: isMoney
        ? (params) => `${params[0].name}<br/>${params[0].seriesName}: ¥${Number(params[0].value).toFixed(2)}`
        : undefined
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1, axisLabel: isMoney ? { formatter: '¥{value}' } : undefined },
    series: [{
      name,
      type: 'line',
      data: values,
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color }
    }]
  })
  return chart
}

const loadTrendData = async () => {
  try {
    const { data } = await getStatsTrend(days.value)
    if (data.code === 200) {
      const trend = data.data
      orderTrendChart?.dispose()
      revenueTrendChart?.dispose()
      await nextTick()
      orderTrendChart = initLineChart(orderTrendRef.value, trend.orderTrend || [], '订单数', '#409EFF')
      revenueTrendChart = initLineChart(revenueTrendRef.value, trend.revenueTrend || [], '收入', '#67C23A', true)
    }
  } catch (e) {
    console.error('Trend load error:', e)
  }
}

const loadStatusData = async () => {
  try {
    const { data } = await getStatsOrderStatus()
    if (data.code === 200) {
      statusDistChart?.dispose()
      await nextTick()
      if (statusDistRef.value) {
        statusDistChart = echarts.init(statusDistRef.value)
        statusDistChart.setOption({
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { orient: 'vertical', left: 'left', top: 'middle' },
          series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            label: { show: false },
            emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
            data: (data.data || []).map(d => ({
              name: d.label || d.LABEL,
              value: Number(d.count || d.COUNT || 0)
            }))
          }]
        })
      }
    }
  } catch (e) {
    console.error('Status load error:', e)
  }
}

const loadTopProducts = async () => {
  try {
    const { data } = await getStats()
    if (data.code === 200) {
      const products = data.data.topProducts || []
      topProductsChart?.dispose()
      await nextTick()
      if (topProductsRef.value && products.length > 0) {
        topProductsChart = echarts.init(topProductsRef.value)
        topProductsChart.setOption({
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'value', minInterval: 1 },
          yAxis: { type: 'category', data: products.map(p => p.name), inverse: true },
          series: [{
            name: '销量',
            type: 'bar',
            data: products.map(p => Number(p.sales || 0)),
            itemStyle: { color: '#E6A23C' }
          }]
        })
      }
    }
  } catch (e) {
    console.error('Top products load error:', e)
  }
}

const handleResize = () => {
  orderTrendChart?.resize()
  revenueTrendChart?.resize()
  statusDistChart?.resize()
  topProductsChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)

  try {
    const { data } = await getStats()
    if (data.code === 200) stats.value = data.data
  } catch (e) {
    console.error('Stats load error:', e)
  }

  await nextTick()
  await Promise.all([loadTrendData(), loadStatusData(), loadTopProducts()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  orderTrendChart?.dispose()
  revenueTrendChart?.dispose()
  statusDistChart?.dispose()
  topProductsChart?.dispose()
})
</script>

<style scoped>
.admin-stats {
  padding: 20px;
}
.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.stat-cards {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.chart-row {
  margin-bottom: 20px;
}
.chart-container {
  width: 100%;
  height: 350px;
}
</style>
