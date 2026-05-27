<template>
  <div class="layout-admin">
    <AdminHeader @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
    <div class="admin-body">
      <AdminSidebar :collapsed="sidebarCollapsed" />
      <main class="admin-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AdminHeader from '../components/AdminHeader.vue'
import AdminSidebar from '../components/AdminSidebar.vue'

const sidebarCollapsed = ref(false)
</script>

<style scoped>
.admin-body { display: flex; }
.admin-content { flex: 1; padding: 20px; background: #f5f7fa; min-height: calc(100vh - 60px); }
</style>
