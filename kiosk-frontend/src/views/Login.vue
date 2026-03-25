<template>
  <div class="login-grid">
    <section class="kiosk-card reveal">
      <p class="eyebrow">用户登录</p>
      <h2>身份证核验上机</h2>
      <p class="subtitle">输入机位编号和身份证后，可直接开始本次上机。</p>

      <div class="field">
        <label for="machine-code">机位编号</label>
        <div class="field-row">
          <input
            id="machine-code"
            v-model="machineCode"
            class="input"
            type="text"
            maxlength="16"
            placeholder="例如 A-027"
            @blur="loadOverview()"
            @keyup.enter="loadOverview()"
          />
          <button class="ghost-btn" :disabled="overviewLoading" @click="loadOverview()">
            {{ overviewLoading ? '读取中...' : '读取机位' }}
          </button>
        </div>
      </div>

      <div class="field">
        <label for="id-card">身份证号码</label>
        <input
          id="id-card"
          v-model="idCard"
          class="input"
          type="text"
          maxlength="18"
          placeholder="请输入身份证号码"
          @keyup.enter="startSession"
        />
      </div>

      <div v-if="errorMessage" class="alert error-alert">{{ errorMessage }}</div>
      <div v-else-if="pausedHint" class="alert">当前机位已暂停锁定，请输入原身份证恢复上机。</div>
      <div v-else class="alert">余额不足 {{ thresholdMinutes }} 分钟时会提醒联系管理员充值。</div>

      <div class="kiosk-actions">
        <button class="solid-btn" :disabled="starting || restoring" @click="startSession">
          {{ starting ? '正在上机...' : '开始上机' }}
        </button>
      </div>
    </section>

    <section class="kiosk-card reveal" style="animation-delay: 0.06s">
      <p class="eyebrow">机位概览</p>
      <h2>{{ overviewTitle }}</h2>
      <p class="subtitle">{{ overviewSubtitle }}</p>

      <div class="overview-status">
        <span class="status-badge" :class="availabilityClass">{{ availabilityText }}</span>
        <span class="status-caption">{{ availabilityMessage }}</span>
      </div>

      <div class="metric-row">
        <span>机位编号</span>
        <span>{{ normalizedMachineCode || '未输入' }}</span>
      </div>
      <div class="metric-row">
        <span>当前单价</span>
        <span>{{ overview?.priceLabel ?? '—' }}</span>
      </div>
      <div class="metric-row">
        <span>状态</span>
        <span>{{ overview?.statusLabel ?? '待查询' }}</span>
      </div>
      <div class="metric-row">
        <span>低余额阈值</span>
        <span>{{ thresholdMinutes }} 分钟</span>
      </div>
    </section>
  </div>

  <section class="kiosk-card reveal" style="animation-delay: 0.12s">
    <p class="eyebrow">上机说明</p>
    <ul class="kiosk-list">
      <li>机位停用或正在被他人使用时，系统会直接阻止开通。</li>
      <li>暂停锁定后，只有原用户重新输入身份证才能恢复上机。</li>
      <li>当前后端只支持联系管理员充值，机位端暂不提供在线续费。</li>
      <li v-if="restoring">正在恢复上次登录状态，请稍候。</li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearKioskSession, hasKioskToken, setKioskSession, setStoredMachineCode } from '../access/kiosk'
import { requestJson } from '../access/http'
import {
  buildMachineQuery,
  type KioskCheckout,
  type KioskLoginResponse,
  type KioskMachineOverview,
  type KioskSessionStatus,
  normalizeMachineCode,
  readErrorMessage,
  readMachineQuery
} from '../kiosk'

const route = useRoute()
const router = useRouter()

const machineCode = ref(readMachineQuery(route.query.machine))
const idCard = ref('')
const overview = ref<KioskMachineOverview | null>(null)
const errorMessage = ref('')
const overviewLoading = ref(false)
const starting = ref(false)
const restoring = ref(false)

const normalizedMachineCode = computed(() => normalizeMachineCode(machineCode.value))
const pausedHint = computed(() => route.query.paused === '1')
const thresholdMinutes = computed(() => overview.value?.lowBalanceThresholdMinutes ?? 60)
const overviewTitle = computed(() => overview.value?.machineCode ?? '等待机位查询')
const overviewSubtitle = computed(() => {
  if (overviewLoading.value) {
    return '正在读取机位当前状态'
  }
  if (overview.value?.status === 3) {
    return '当前机位处于暂停锁定状态，原用户可输入身份证直接恢复'
  }
  return overview.value ? '实时读取机位状态、单价和可用性' : '先输入机位编号，再读取当前机位状态'
})
const availabilityText = computed(() => {
  if (!overview.value) {
    return '待查询'
  }
  return overview.value.available ? '可上机' : overview.value.statusLabel
})
const availabilityMessage = computed(() => overview.value?.availabilityMessage ?? '机位状态未读取')
const availabilityClass = computed(() => {
  if (!overview.value) {
    return 'status-pending'
  }
  if (overview.value.available) {
    return 'status-ready'
  }
  if (overview.value.status === 3) {
    return 'status-locked'
  }
  return overview.value.status === 2 ? 'status-disabled' : 'status-busy'
})

const goToStatus = async (machine: string) => {
  await router.replace({
    name: 'kiosk-status',
    query: buildMachineQuery(machine)
  })
}

const goToCheckout = async (machine: string) => {
  await router.replace({
    name: 'kiosk-checkout',
    query: buildMachineQuery(machine)
  })
}

const restoreFlow = async () => {
  if (!hasKioskToken()) {
    return
  }

  restoring.value = true
  errorMessage.value = ''

  try {
    const current = await requestJson<KioskSessionStatus | null>('/kiosk/sessions/current')
    if (current) {
      setStoredMachineCode(current.machineCode)
      await goToStatus(current.machineCode)
      return
    }

    const latestEnded = await requestJson<KioskCheckout | null>('/kiosk/sessions/latest-ended')
    if (latestEnded) {
      setStoredMachineCode(latestEnded.machineCode)
      await goToCheckout(latestEnded.machineCode)
      return
    }

    clearKioskSession()
  } catch {
    clearKioskSession()
  } finally {
    restoring.value = false
  }
}

const loadOverview = async () => {
  if (!normalizedMachineCode.value) {
    overview.value = null
    errorMessage.value = '请先输入机位编号'
    return
  }

  overviewLoading.value = true
  errorMessage.value = ''

  try {
    const data = await requestJson<KioskMachineOverview>(
      `/kiosk/machines/${encodeURIComponent(normalizedMachineCode.value)}/overview`
    )
    overview.value = data
    machineCode.value = data.machineCode
    setStoredMachineCode(data.machineCode)
  } catch (error) {
    overview.value = null
    errorMessage.value = readErrorMessage(error, '机位信息获取失败')
  } finally {
    overviewLoading.value = false
  }
}

const startSession = async () => {
  if (!normalizedMachineCode.value) {
    errorMessage.value = '请输入机位编号'
    return
  }
  if (!idCard.value.trim()) {
    errorMessage.value = '请输入身份证号码'
    return
  }

  starting.value = true
  errorMessage.value = ''

  try {
    const response = await requestJson<KioskLoginResponse>('/kiosk/sessions/start', {
      method: 'POST',
      body: JSON.stringify({
        machineCode: normalizedMachineCode.value,
        idCard: idCard.value.trim()
      })
    })
    setKioskSession({
      token: response.token,
      user: response.user
    })
    setStoredMachineCode(response.machine.machineCode)
    await goToStatus(response.machine.machineCode)
  } catch (error) {
    errorMessage.value = readErrorMessage(error, '上机失败，请稍后重试')
  } finally {
    starting.value = false
  }
}

onMounted(async () => {
  await restoreFlow()
  if (!restoring.value && normalizedMachineCode.value && !hasKioskToken()) {
    await loadOverview()
  }
})
</script>

<style scoped>
.login-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 0.8fr);
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field label {
  font-size: 13px;
  color: var(--muted);
}

.field-row {
  display: grid;
  gap: 10px;
  grid-template-columns: minmax(0, 1fr) auto;
}

.overview-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(23, 23, 23, 0.03);
}

.status-badge {
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-ready {
  background: rgba(28, 170, 88, 0.14);
  color: #0d8a42;
}

.status-busy {
  background: rgba(33, 110, 214, 0.14);
  color: #1d6fd1;
}

.status-disabled {
  background: rgba(23, 23, 23, 0.1);
  color: #5b5853;
}

.status-locked {
  background: rgba(255, 138, 0, 0.18);
  color: #a85a00;
}

.status-pending {
  background: rgba(255, 138, 0, 0.16);
  color: #a85a00;
}

.status-caption {
  color: var(--muted);
  font-size: 13px;
}

.error-alert {
  background: rgba(224, 62, 62, 0.12);
  color: #b33838;
}

.kiosk-list {
  display: grid;
  gap: 8px;
  color: var(--muted);
  font-size: 14px;
  padding-left: 18px;
}

@media (max-width: 960px) {
  .login-grid {
    grid-template-columns: 1fr;
  }

  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
