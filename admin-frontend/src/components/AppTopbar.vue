<template>
  <header class="topbar topbar-crumbs">
    <div class="crumb-block">
      <nav class="crumbs" aria-label="页面路径">
        <span class="crumb-link">运营控制台</span>
        <span class="crumb-separator">/</span>
        <span class="crumb-link">{{ eyebrow }}</span>
        <span class="crumb-separator">/</span>
        <strong class="crumb-current">{{ title }}</strong>
      </nav>
      <p v-if="subtitle" class="crumb-subtitle">{{ subtitle }}</p>
    </div>
  </header>
</template>

<script setup lang="ts">
import { onUnmounted, watchEffect } from 'vue'
import { clearAdminToolbarAction, setAdminToolbarAction } from '../layouts/adminToolbar'

const props = withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    eyebrow?: string
    actionLabel?: string
    actionDisabled?: boolean
    actionHandler?: () => void | Promise<void>
  }>(),
  {
    eyebrow: '管理中心',
    actionLabel: '',
    actionDisabled: false,
    actionHandler: undefined
  }
)

watchEffect(() => {
  if (!props.actionLabel || !props.actionHandler) {
    clearAdminToolbarAction()
    return
  }
  setAdminToolbarAction({
    label: props.actionLabel,
    disabled: props.actionDisabled,
    handler: props.actionHandler
  })
})

onUnmounted(() => {
  clearAdminToolbarAction()
})
</script>

<style scoped>
.topbar-crumbs {
  align-items: flex-start;
}

.crumb-block {
  display: grid;
  gap: 8px;
}

.crumbs {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.crumb-link,
.crumb-separator {
  color: var(--muted);
  font-size: 13px;
  letter-spacing: 0.06em;
}

.crumb-current {
  color: var(--ink);
  font-family: var(--font-display);
  font-size: 24px;
  letter-spacing: 0.03em;
}

.crumb-subtitle {
  color: var(--muted);
  font-size: 14px;
}
</style>
