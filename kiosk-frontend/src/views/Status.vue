<template>
  <div class="kiosk-card reveal">
    <p class="eyebrow">当前状态</p>
    <div class="status-head">
      <h2>{{ statusTitle }}</h2>
      <span class="status-chip" :class="statusClass">{{ statusLabel }}</span>
    </div>
    <div class="kiosk-metric">01:26:24</div>
    <div class="metric-row">
      <span>机位</span>
      <span>A-027</span>
    </div>
    <div class="metric-row">
      <span>单价</span>
      <span>¥0.12/分钟</span>
    </div>
    <div class="metric-row">
      <span>已计费分钟</span>
      <span>86</span>
    </div>
    <div class="metric-row">
      <span>已消费</span>
      <span>¥10.32</span>
    </div>
  </div>

  <div class="kiosk-card reveal" style="animation-delay: 0.08s">
    <p class="eyebrow">余额与操作</p>
    <div class="kiosk-metric">¥86.40</div>
    <p class="subtitle">预计可用 720 分钟</p>
    <div class="alert">暂停期间计费停止，解锁需再次输入身份证。</div>
    <div class="kiosk-actions">
      <button class="solid-btn" @click="togglePause">{{ actionLabel }}</button>
      <button class="ghost-btn">下机</button>
    </div>
  </div>

  <div v-if="isPaused" class="kiosk-card reveal" style="animation-delay: 0.12s">
    <p class="eyebrow">暂停解锁</p>
    <h2>请输入身份证继续上机</h2>
    <input class="input" placeholder="身份证号码" />
    <div class="kiosk-actions">
      <button class="solid-btn">继续使用</button>
      <button class="ghost-btn" @click="togglePause">取消</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const isPaused = ref(false)

const statusLabel = computed(() => (isPaused.value ? '暂停中' : '计费中'))
const statusTitle = computed(() => (isPaused.value ? '已暂停' : '正在上机'))
const statusClass = computed(() => (isPaused.value ? 'paused' : 'active'))
const actionLabel = computed(() => (isPaused.value ? '继续计费' : '暂停计费'))

const togglePause = () => {
  isPaused.value = !isPaused.value
}
</script>

<style scoped>
.status-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-chip {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-chip.active {
  background: rgba(29, 111, 209, 0.15);
  color: #1d6fd1;
}

.status-chip.paused {
  background: rgba(255, 138, 0, 0.18);
  color: #b45a00;
}
</style>
