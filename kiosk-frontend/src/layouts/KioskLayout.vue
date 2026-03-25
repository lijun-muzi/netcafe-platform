<template>
  <div class="kiosk-shell">
    <header class="kiosk-header">
      <div>
        <p class="eyebrow">机位端 · 自助上机</p>
        <h1>星河网咖 · 机位控制台</h1>
      </div>
      <div class="kiosk-chip">{{ machineLabel }}</div>
    </header>
    <div class="kiosk-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getStoredMachineCode } from '../access/kiosk'
import { readMachineQuery } from '../kiosk'

const route = useRoute()

const machineLabel = computed(() => {
  const machineCode = readMachineQuery(route.query.machine) || getStoredMachineCode()
  return machineCode ? `机器编号 ${machineCode}` : '未绑定机位'
})
</script>
