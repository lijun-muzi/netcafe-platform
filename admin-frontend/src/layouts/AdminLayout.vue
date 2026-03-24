<template>
  <div class="admin-shell">
    <AppSidebar />
    <main class="admin-main">
      <div class="admin-hero">
        <div>
          <p class="eyebrow">网吧管理系统</p>
          <h1>运营控制台</h1>
        </div>
        <div class="hero-actions">
          <button class="ghost-btn">今日班次 09:00 - 18:00</button>
        </div>
      </div>
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from '../components/AppSidebar.vue'
</script>
