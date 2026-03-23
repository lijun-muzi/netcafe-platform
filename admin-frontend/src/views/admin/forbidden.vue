<template>
  <section class="page">
    <div class="card">
      <p class="eyebrow">权限控制</p>
      <h2>无权访问该页面</h2>
      <p class="subtitle">当前角色没有权限进入此模块，请联系超级管理员。</p>
      <button class="ghost-btn" @click="goBack">返回首页</button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const goBack = () => {
  router.push('/admin/dashboard')
}
</script>
