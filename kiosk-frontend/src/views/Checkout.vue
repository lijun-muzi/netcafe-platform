<template>
  <div class="checkout-grid">
    <section class="kiosk-card reveal">
      <p class="eyebrow">{{ mode === 'preview' ? '下机确认' : '结算完成' }}</p>
      <h2>{{ titleText }}</h2>
      <p class="subtitle">{{ subtitleText }}</p>

      <div v-if="errorMessage" class="alert error-alert">{{ errorMessage }}</div>

      <template v-if="checkout">
        <div class="summary-block">
          <div class="summary-row">
            <span>用户</span>
            <strong>{{ checkout.userName }}</strong>
          </div>
          <div class="summary-row">
            <span>机位</span>
            <strong>{{ checkout.machineCode }}</strong>
          </div>
          <div class="summary-row">
            <span>开始时间</span>
            <strong>{{ formatDateTime(checkout.startTime) }}</strong>
          </div>
          <div class="summary-row">
            <span>结束时间</span>
            <strong>{{ formatDateTime(checkout.endTime) }}</strong>
          </div>
        </div>

        <div class="checkout-amounts">
          <div class="amount-card">
            <span>本次时长</span>
            <strong>{{ checkout.durationLabel }}</strong>
          </div>
          <div class="amount-card">
            <span>订单总额</span>
            <strong>{{ checkout.totalAmountLabel }}</strong>
          </div>
          <div class="amount-card accent-card">
            <span>{{ mode === 'preview' ? '待扣金额' : '本次结算' }}</span>
            <strong>{{ checkout.settlementAmountLabel }}</strong>
          </div>
        </div>

        <div class="summary-block">
          <div class="summary-row">
            <span>机位单价</span>
            <strong>{{ checkout.priceLabel }}</strong>
          </div>
          <div class="summary-row">
            <span>结算前余额</span>
            <strong>{{ checkout.balanceBeforeLabel }}</strong>
          </div>
          <div class="summary-row">
            <span>结算后余额</span>
            <strong>{{ checkout.balanceAfterLabel }}</strong>
          </div>
          <div class="summary-row">
            <span>订单状态</span>
            <strong>{{ checkout.statusLabel }}</strong>
          </div>
        </div>
      </template>

      <div class="kiosk-actions">
        <button
          v-if="mode === 'preview'"
          class="solid-btn"
          :disabled="loading || ending || !checkout"
          @click="confirmEnd"
        >
          {{ ending ? '正在下机...' : '确认下机' }}
        </button>
        <button
          v-if="mode === 'preview'"
          class="ghost-btn"
          :disabled="loading || ending"
          @click="backToStatus"
        >
          返回状态页
        </button>
        <button v-else class="solid-btn" :disabled="loading" @click="finishFlow">完成并返回首页</button>
      </div>
    </section>

    <section class="kiosk-card reveal" style="animation-delay: 0.08s">
      <p class="eyebrow">温馨提示</p>
      <h2>下机前请确认</h2>
      <ul class="checkout-list">
        <li>当前页面显示的是本次下机的预计结算信息，确认后会立即停止计费。</li>
        <li>下机完成后，页面会显示本次消费结果，方便你核对扣费和余额变化。</li>
        <li>如果对结算金额有疑问，请联系前台管理员处理。</li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearKioskSession, getStoredMachineCode, setStoredMachineCode } from '../access/kiosk'
import { ApiError, requestJson } from '../access/http'
import {
  buildMachineQuery,
  formatDateTime,
  type KioskCheckout,
  readErrorMessage,
  readMachineQuery
} from '../kiosk'

const route = useRoute()
const router = useRouter()

const checkout = ref<KioskCheckout | null>(null)
const mode = ref<'preview' | 'receipt'>('preview')
const loading = ref(true)
const ending = ref(false)
const errorMessage = ref('')

const machineCode = computed(() => readMachineQuery(route.query.machine) || getStoredMachineCode() || '')
const titleText = computed(() => (mode.value === 'preview' ? '确认结束本次上机' : '本次上机已完成结算'))
const subtitleText = computed(() => {
  return mode.value === 'preview'
    ? '确认后将停止计费并立刻生成最终订单。'
    : '这是后端返回的最近一笔结算回执，可用于用户确认扣费。'
})

const goLogin = async () => {
  clearKioskSession()
  await router.replace({
    name: 'kiosk-login',
    query: buildMachineQuery(machineCode.value)
  })
}

const loadLatestEnded = async () => {
  const latestEnded = await requestJson<KioskCheckout | null>('/kiosk/sessions/latest-ended')
  if (!latestEnded) {
    return false
  }
  checkout.value = latestEnded
  mode.value = 'receipt'
  setStoredMachineCode(latestEnded.machineCode)
  errorMessage.value = ''
  return true
}

const loadCheckout = async () => {
  loading.value = true

  try {
    const preview = await requestJson<KioskCheckout>('/kiosk/sessions/checkout-preview')
    checkout.value = preview
    mode.value = 'preview'
    setStoredMachineCode(preview.machineCode)
    errorMessage.value = ''
    return
  } catch (error) {
    try {
      const loaded = await loadLatestEnded()
      if (loaded) {
        return
      }
    } catch (latestError) {
      if (latestError instanceof ApiError && latestError.status === 401) {
        await goLogin()
        return
      }
    }

    if (error instanceof ApiError && error.status === 401) {
      await goLogin()
      return
    }

    errorMessage.value = readErrorMessage(error, '结算信息获取失败')
  } finally {
    loading.value = false
  }
}

const confirmEnd = async () => {
  if (!checkout.value) {
    return
  }

  ending.value = true
  errorMessage.value = ''

  try {
    const ended = await requestJson<KioskCheckout>('/kiosk/sessions/end', {
      method: 'POST'
    })
    checkout.value = ended
    mode.value = 'receipt'
    setStoredMachineCode(ended.machineCode)
  } catch (error) {
    errorMessage.value = readErrorMessage(error, '下机失败，请稍后重试')
    if (error instanceof ApiError && error.status === 401) {
      await goLogin()
    }
  } finally {
    ending.value = false
  }
}

const backToStatus = async () => {
  await router.push({
    name: 'kiosk-status',
    query: buildMachineQuery(checkout.value?.machineCode ?? machineCode.value)
  })
}

const finishFlow = async () => {
  await goLogin()
}

onMounted(async () => {
  await loadCheckout()
})
</script>

<style scoped>
.checkout-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
}

.summary-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(23, 23, 23, 0.03);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
  color: var(--muted);
}

.summary-row strong {
  color: var(--ink);
}

.checkout-amounts {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.amount-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(23, 23, 23, 0.03);
}

.amount-card span {
  font-size: 12px;
  color: var(--muted);
}

.amount-card strong {
  font-size: 20px;
}

.accent-card {
  background: linear-gradient(135deg, rgba(255, 138, 0, 0.16), rgba(255, 225, 189, 0.7));
}

.checkout-list {
  display: grid;
  gap: 8px;
  color: var(--muted);
  font-size: 14px;
  padding-left: 18px;
}

.error-alert {
  background: rgba(224, 62, 62, 0.12);
  color: #b33838;
}

@media (max-width: 960px) {
  .checkout-grid {
    grid-template-columns: 1fr;
  }

  .checkout-amounts {
    grid-template-columns: 1fr;
  }

  .summary-row {
    flex-direction: column;
  }
}
</style>
