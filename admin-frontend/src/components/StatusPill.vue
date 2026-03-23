<template>
  <span class="status-pill" :class="toneClass">{{ text }}</span>
</template>

<script setup lang="ts">
const props = defineProps<{
  text: string
  tone: 'idle' | 'using' | 'disabled' | 'forced' | 'info'
}>()

const toneClass = `pill-${props.tone}`
</script>
