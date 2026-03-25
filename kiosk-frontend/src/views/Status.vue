<template>
  <div class="status-grid">
    <section class="kiosk-card reveal">
      <p class="eyebrow">当前状态</p>
      <div class="card-head">
        <div>
          <h2>{{ session?.userName ?? '正在读取状态' }}</h2>
          <p class="subtitle">{{ machineLabel }}</p>
        </div>
        <span class="status-chip" :class="statusClass">{{ session?.statusLabel ?? '读取中' }}</span>
      </div>

      <div class="kiosk-metric">{{ liveDurationLabel }}</div>

      <div v-if="errorMessage" class="alert error-alert">{{ errorMessage }}</div>
      <div v-else-if="session?.paused" class="alert">{{ pauseAlertMessage }}</div>
      <div v-else-if="session?.lowBalanceWarning" class="alert">{{ session.lowBalanceMessage }}</div>
      <div v-else class="alert success-alert">系统每 15 秒自动刷新一次当前状态。</div>

      <div class="metric-grid">
        <div class="metric-tile">
          <span>开始时间</span>
          <strong>{{ session ? formatDateTime(session.startTime) : '—' }}</strong>
        </div>
        <div class="metric-tile">
          <span>{{ session?.paused ? '暂停时间' : '当前费用' }}</span>
          <strong>{{ session?.paused ? formatDateTime(session.pausedAt) : session?.currentFeeLabel ?? '—' }}</strong>
        </div>
        <div class="metric-tile">
          <span>已结转</span>
          <strong>{{ session?.billedAmountLabel ?? '—' }}</strong>
        </div>
        <div class="metric-tile">
          <span>当前余额</span>
          <strong>{{ session?.balanceLabel ?? '—' }}</strong>
        </div>
      </div>
    </section>

    <section class="kiosk-card reveal" style="animation-delay: 0.06s">
      <p class="eyebrow">余额与结算</p>
      <h2>本次上机摘要</h2>

      <div class="metric-row">
        <span>机位单价</span>
        <span>{{ session?.priceLabel ?? '—' }}</span>
      </div>
      <div class="metric-row">
        <span>已计费分钟</span>
        <span>{{ session?.billedMinutes ?? 0 }} 分钟</span>
      </div>
      <div class="metric-row">
        <span>预计剩余时长</span>
        <span>{{ session?.remainingMinutesLabel ?? '—' }}</span>
      </div>
      <div class="metric-row">
        <span>低余额提醒</span>
        <span>{{ session?.lowBalanceThresholdMinutes ?? '—' }} 分钟</span>
      </div>

      <div class="kiosk-actions">
        <button class="ghost-btn" :disabled="loading || pausing" @click="loadSession(false)">
          {{ loading ? '刷新中...' : '手动刷新' }}
        </button>
        <button
          v-if="!session?.paused"
          class="ghost-btn"
          :disabled="loading || pausing || !session"
          @click="pauseSession"
        >
          {{ pausing ? '暂停中...' : '暂停上机' }}
        </button>
        <button
          v-else
          class="ghost-btn"
          :disabled="loading"
          @click="goResumeLogin"
        >
          返回登录页恢复
        </button>
        <button class="solid-btn" :disabled="loading || pausing || !session" @click="goCheckout">立即下机</button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearKioskSession, getStoredMachineCode, setStoredMachineCode } from '../access/kiosk'
import { ApiError, requestJson } from '../access/http'
import {
  buildMachineQuery,
  formatDateTime,
  type KioskCheckout,
  type KioskSessionStatus,
  readErrorMessage,
  readMachineQuery
} from '../kiosk'

const route = useRoute()
const router = useRouter()

const session = ref<KioskSessionStatus | null>(null)
const loading = ref(true)
const pausing = ref(false)
const errorMessage = ref('')
const clockNow = ref(Date.now())
let pollTimer: number | null = null
let clockTimer: number | null = null

const machineQueryCode = computed(() => readMachineQuery(route.query.machine) || getStoredMachineCode() || '')
const machineLabel = computed(() => (session.value?.machineCode ?? machineQueryCode.value) || '未绑定机位')
const statusClass = computed(() => {
  if (!session.value) {
    return 'status-loading'
  }
  if (session.value.paused) {
    return 'status-paused'
  }
  return session.value.lowBalanceWarning ? 'status-warning' : 'status-active'
})
const pauseAlertMessage = computed(() => {
  if (!session.value?.paused) {
    return ''
  }
  const pausedAt = formatDateTime(session.value.pausedAt)
  return `已于 ${pausedAt} 暂停上机。返回登录页后，输入原身份证即可恢复。`
})

const resolveElapsedSeconds = (startTime: string | null | undefined) => {
  if (!startTime) {
    return 0
  }
  const parsedTime = new Date(startTime).getTime()
  if (Number.isNaN(parsedTime)) {
    return (session.value?.currentDurationMinutes ?? 0) * 60
  }
  if (session.value?.paused && session.value.pausedAt) {
    const pausedTime = new Date(session.value.pausedAt).getTime()
    if (!Number.isNaN(pausedTime)) {
      return Math.max(0, Math.floor((pausedTime - parsedTime) / 1000))
    }
  }
  return Math.max(0, Math.floor((clockNow.value - parsedTime) / 1000))
}

const formatElapsed = (totalSeconds: number) => {
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  return [hours, minutes, seconds].map((value) => String(value).padStart(2, '0')).join(':')
}

const liveDurationLabel = computed(() => {
  if (!session.value) {
    return '—'
  }
  return formatElapsed(resolveElapsedSeconds(session.value.startTime))
})

const goLogin = async () => {
  clearKioskSession()
  await router.replace({
    name: 'kiosk-login',
    query: buildMachineQuery(machineQueryCode.value)
  })
}

const goResumeLogin = async () => {
  const machineCode = session.value?.machineCode || machineQueryCode.value
  clearKioskSession()
  await router.replace({
    name: 'kiosk-login',
    query: {
      ...buildMachineQuery(machineCode),
      paused: '1'
    }
  })
}

const routeToCheckoutIfNeeded = async () => {
  try {
    const latestEnded = await requestJson<KioskCheckout | null>('/kiosk/sessions/latest-ended')
    if (latestEnded) {
      setStoredMachineCode(latestEnded.machineCode)
      await router.replace({
        name: 'kiosk-checkout',
        query: buildMachineQuery(latestEnded.machineCode)
      })
      return true
    }
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      await goLogin()
      return true
    }
  }

  await goLogin()
  return true
}

const loadSession = async (silent = false) => {
  if (!silent) {
    loading.value = true
  }

  try {
    const current = await requestJson<KioskSessionStatus | null>('/kiosk/sessions/current')
    if (!current) {
      await routeToCheckoutIfNeeded()
      return
    }
    session.value = current
    errorMessage.value = ''
    setStoredMachineCode(current.machineCode)
  } catch (error) {
    const message = readErrorMessage(error, '当前状态获取失败')
    if (!silent) {
      errorMessage.value = message
    }
    if (error instanceof ApiError && error.status === 401) {
      await goLogin()
    }
  } finally {
    loading.value = false
  }
}

const goCheckout = async () => {
  const machineCode = session.value?.machineCode || machineQueryCode.value
  await router.push({
    name: 'kiosk-checkout',
    query: buildMachineQuery(machineCode)
  })
}

const pauseSession = async () => {
  if (!session.value || pausing.value) {
    return
  }

  pausing.value = true
  errorMessage.value = ''

  try {
    const pausedSession = await requestJson<KioskSessionStatus>('/kiosk/sessions/pause', {
      method: 'POST'
    })
    session.value = pausedSession
    setStoredMachineCode(pausedSession.machineCode)
    await goResumeLogin()
  } catch (error) {
    errorMessage.value = readErrorMessage(error, '暂停上机失败')
    if (error instanceof ApiError && error.status === 401) {
      await goLogin()
    }
  } finally {
    pausing.value = false
  }
}

const startPolling = () => {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer)
  }
  pollTimer = window.setInterval(() => {
    void loadSession(true)
  }, 15000)
}

const startClock = () => {
  if (clockTimer !== null) {
    window.clearInterval(clockTimer)
  }
  clockNow.value = Date.now()
  clockTimer = window.setInterval(() => {
    clockNow.value = Date.now()
  }, 1000)
}

onMounted(async () => {
  await loadSession()
  startPolling()
  startClock()
})

onBeforeUnmount(() => {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer)
  }
  if (clockTimer !== null) {
    window.clearInterval(clockTimer)
  }
})
</script>

<style scoped>
.status-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.status-chip {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-active {
  background: rgba(29, 111, 209, 0.14);
  color: #1d6fd1;
}

.status-warning {
  background: rgba(255, 138, 0, 0.18);
  color: #a85a00;
}

.status-paused {
  background: rgba(255, 138, 0, 0.18);
  color: #a85a00;
}

.status-loading {
  background: rgba(23, 23, 23, 0.08);
  color: #6b6762;
}

.metric-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.metric-tile {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(23, 23, 23, 0.03);
}

.metric-tile span {
  font-size: 12px;
  color: var(--muted);
}

.metric-tile strong {
  font-size: 18px;
}

.error-alert {
  background: rgba(224, 62, 62, 0.12);
  color: #b33838;
}

.success-alert {
  background: rgba(29, 154, 163, 0.12);
  color: #0e6f76;
}

@media (max-width: 960px) {
  .status-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
