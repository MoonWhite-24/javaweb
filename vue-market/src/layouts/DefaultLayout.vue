<template>
  <div class="layout-default">
    <Navbar />
    <main class="main-content">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Navbar from '../components/Navbar.vue'
import Footer from '../components/Footer.vue'
</script>

<style scoped>
.main-content { min-height: calc(100vh - 120px); padding-top: 60px; background: #f5f7fa; }
</style>
