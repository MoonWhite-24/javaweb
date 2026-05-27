<template>
  <el-menu :collapse="collapsed" router class="admin-sidebar" default-active="/admin">
    <el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon><span>仪表盘</span></el-menu-item>
    <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
    <el-menu-item index="/admin/orders"><el-icon><Document /></el-icon><span>订单管理</span></el-menu-item>
    <el-menu-item index="/admin/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
    <el-menu-item index="/admin/seckill"><el-icon><Timer /></el-icon><span>秒杀管理</span></el-menu-item>
    <el-menu-item index="/admin/stats"><el-icon><TrendCharts /></el-icon><span>数据统计</span></el-menu-item>
  </el-menu>
</template>

<script setup>
defineProps({ collapsed: Boolean })
</script>

<style scoped>
.admin-sidebar { min-height: calc(100vh - 60px); }
</style>
