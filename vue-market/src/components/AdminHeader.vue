<template>
  <div class="admin-header">
    <el-button @click="$emit('toggleSidebar')" text><el-icon><Menu /></el-icon></el-button>
    <span class="title">管理后台</span>
    <div class="flex-grow" />
    <el-button @click="router.push('/')" text>返回前台</el-button>
  </div>
</template>

<script setup>
import { Menu } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
const router = useRouter()
defineEmits(['toggleSidebar'])
</script>

<style scoped>
.admin-header { display: flex; align-items: center; padding: 0 16px; height: 60px; background: #fff; border-bottom: 1px solid #e4e7ed; }
.title { font-size: 18px; font-weight: bold; margin-left: 12px; }
.flex-grow { flex-grow: 1; }
</style>
