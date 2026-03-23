<template>
  <div class="stat-card" :class="toneClass">
    <p class="stat-label">{{ label }}</p>
    <div class="stat-value">
      <span>{{ value }}</span>
      <small v-if="suffix">{{ suffix }}</small>
    </div>
    <p v-if="note" class="stat-note">{{ note }}</p>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  label: string
  value: string
  suffix?: string
  note?: string
  tone?: 'neutral' | 'success' | 'warning' | 'info'
}>()

const toneClass = props.tone ? `stat-${props.tone}` : 'stat-neutral'
</script>
