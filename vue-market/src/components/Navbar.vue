<template>
  <el-menu mode="horizontal" :ellipsis="false" class="navbar" router>
    <router-link to="/" class="logo"><el-icon :size="24"><ShoppingCartFull /></el-icon><span>JavaWeb Mall</span></router-link>
    <div class="flex-grow" />
    <el-menu-item index="/">首页</el-menu-item>
    <el-menu-item index="/products">全部商品</el-menu-item>
    <el-menu-item index="/seckill">秒杀</el-menu-item>
    <el-menu-item v-if="!userStore.isLoggedIn" index="/login">登录</el-menu-item>
    <el-menu-item v-if="!userStore.isLoggedIn" index="/register">注册</el-menu-item>
    <el-sub-menu v-if="userStore.isLoggedIn" index="user-menu">
      <template #title>{{ userStore.userInfo?.username }}</template>
      <el-menu-item index="/profile">个人中心</el-menu-item>
      <el-menu-item index="/orders">我的订单</el-menu-item>
      <el-menu-item v-if="userStore.isAdmin" index="/admin">后台管理</el-menu-item>
      <el-menu-item @click="userStore.logout()">退出登录</el-menu-item>
    </el-sub-menu>
    <el-menu-item index="/cart">
      <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount">
        <el-icon><ShoppingCart /></el-icon>
      </el-badge>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { onMounted } from 'vue'

const userStore = useUserStore()
const cartStore = useCartStore()

onMounted(() => { if (userStore.isLoggedIn) cartStore.fetch() })
</script>

<style scoped>
.navbar { position: fixed; top: 0; width: 100%; z-index: 1000; }
.logo { display: flex; align-items: center; gap: 8px; padding: 0 20px; text-decoration: none; font-size: 20px; font-weight: 800; background: linear-gradient(135deg, #409eff, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.flex-grow { flex-grow: 1; }
</style>
