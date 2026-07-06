<template>
  <AdminLayout>
    <div class="dashboard">
      <h2>仪表盘</h2>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stat-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-blue">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">今日订单</div>
                <div class="stat-value">{{ stats.todayOrderCount || 0 }}</div>
              </div>
              <el-icon class="stat-icon"><Document /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-green">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">今日收入</div>
                <div class="stat-value">¥{{ formatMoney(stats.todayRevenue) }}</div>
              </div>
              <el-icon class="stat-icon"><Money /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-purple">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">今日新增用户</div>
                <div class="stat-value">{{ stats.todayNewUsers || 0 }}</div>
              </div>
              <el-icon class="stat-icon"><UserFilled /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-orange">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">待付款订单</div>
                <div class="stat-value">{{ stats.unpaidOrders || 0 }}</div>
              </div>
              <el-icon class="stat-icon"><Clock /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="stat-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-cyan">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">总商品数</div>
                <div class="stat-value">{{ stats.totalProducts || 0 }}</div>
              </div>
              <el-icon class="stat-icon"><Goods /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-red">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">总用户数</div>
                <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
              </div>
              <el-icon class="stat-icon"><User /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-yellow">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">秒杀商品数</div>
                <div class="stat-value">{{ stats.seckillCount || 0 }}</div>
              </div>
              <el-icon class="stat-icon"><Timer /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card card-teal">
            <div class="stat-card-content">
              <div class="stat-info">
                <div class="stat-title">热销商品</div>
                <div class="stat-value">{{ stats.topProducts ? stats.topProducts.length : 0 }} 款</div>
              </div>
              <el-icon class="stat-icon"><TrendCharts /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="14">
          <el-card shadow="hover">
            <template #header>
              <span>近 7 天订单趋势</span>
            </template>
            <div ref="orderChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="hover">
            <template #header>
              <span>订单状态分布</span>
            </template>
            <div ref="statusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :span="14">
          <el-card shadow="hover">
            <template #header>
              <span>近 7 天收入趋势</span>
            </template>
            <div ref="revenueChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="hover">
            <template #header>
              <span>热销商品 TOP 5</span>
            </template>
            <div ref="topProductsChartRef" class="chart-container"></div>
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
import { Document, Money, UserFilled, Clock, Goods, User, Timer, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref({})
const orderChartRef = ref(null)
const statusChartRef = ref(null)
const revenueChartRef = ref(null)
const topProductsChartRef = ref(null)

let orderChart = null
let statusChart = null
let revenueChart = null
let topProductsChart = null

const formatMoney = (val) => {
  if (!val) return '0.00'
  return Number(val).toFixed(2)
}

const initOrderChart = (data) => {
  if (!orderChartRef.value) return
  orderChart = echarts.init(orderChartRef.value)
  const dates = data.map(d => d.date || d.DATE || '')
  const counts = data.map(d => Number(d.count || d.COUNT || 0))
  orderChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '订单数',
      type: 'line',
      data: counts,
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

const initRevenueChart = (data) => {
  if (!revenueChartRef.value) return
  revenueChart = echarts.init(revenueChartRef.value)
  const dates = data.map(d => d.date || d.DATE || '')
  const revenues = data.map(d => Number(d.revenue || d.REVENUE || 0))
  revenueChart.setOption({
    tooltip: { trigger: 'axis', formatter: (params) => `${params[0].name}<br/>${params[0].seriesName}: ¥${Number(params[0].value).toFixed(2)}` },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [{
      name: '收入',
      type: 'line',
      data: revenues,
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#67C23A' }
    }]
  })
}

const initStatusChart = (data) => {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.map(d => ({ name: d.label || d.LABEL, value: Number(d.count || d.COUNT || 0) }))
    }]
  })
}

const initTopProductsChart = (data) => {
  if (!topProductsChartRef.value || !data || data.length === 0) return
  topProductsChart = echarts.init(topProductsChartRef.value)
  const names = data.map(p => p.name)
  const sales = data.map(p => Number(p.sales || 0))
  topProductsChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: names, inverse: true },
    series: [{
      name: '销量',
      type: 'bar',
      data: sales,
      itemStyle: { color: '#E6A23C' }
    }]
  })
}

const handleResize = () => {
  orderChart?.resize()
  statusChart?.resize()
  revenueChart?.resize()
  topProductsChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)

  try {
    const [statsRes, trendRes, statusRes] = await Promise.all([
      getStats(),
      getStatsTrend(7),
      getStatsOrderStatus()
    ])

    if (statsRes.data.code === 200) {
      stats.value = statsRes.data.data
    }

    await nextTick()

    if (trendRes.data.code === 200) {
      const trendData = trendRes.data.data
      initOrderChart(trendData.orderTrend || [])
      initRevenueChart(trendData.revenueTrend || [])
    }

    if (statusRes.data.code === 200) {
      initStatusChart(statusRes.data.data || [])
    }

    if (stats.value.topProducts) {
      initTopProductsChart(stats.value.topProducts)
    }
  } catch (e) {
    console.error('Dashboard load error:', e)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  orderChart?.dispose()
  statusChart?.dispose()
  revenueChart?.dispose()
  topProductsChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}
.stat-cards {
  margin-bottom: 20px;
}
.stat-card {
  border-radius: 8px;
}
.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-info {
  flex: 1;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-icon {
  font-size: 48px;
  opacity: 0.15;
}
.card-blue .stat-icon { color: #409EFF; }
.card-green .stat-icon { color: #67C23A; }
.card-purple .stat-icon { color: #9B59B6; }
.card-orange .stat-icon { color: #E6A23C; }
.card-cyan .stat-icon { color: #00BCD4; }
.card-red .stat-icon { color: #F56C6C; }
.card-yellow .stat-icon { color: #F7BA2A; }
.card-teal .stat-icon { color: #20B2AA; }
.chart-row {
  margin-bottom: 20px;
}
.chart-container {
  width: 100%;
  height: 300px;
}
</style>
