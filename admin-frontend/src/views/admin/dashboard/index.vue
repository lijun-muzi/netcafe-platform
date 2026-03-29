<template>
  <section class="page dashboard-page">
    <AppTopbar
      title="实时面板"
      subtitle="机位运行状态、当前会话与营业概览"
      eyebrow="首页"
      action-label="立即刷新"
      :action-disabled="loading || submitting"
      :action-handler="refreshAll"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">机位筛选</p>
          <h3>编号 / 状态 / 单价区间</h3>
        </div>
        <div class="filter-bar">
          <input
            v-model.trim="filters.keyword"
            class="input"
            placeholder="机位编号 / 区域 / 配置"
            @keyup.enter="applyFilters"
          />
          <select v-model="filters.status" class="select">
            <option value="">全部状态</option>
            <option value="0">空闲</option>
            <option value="1">使用中</option>
            <option value="2">停用</option>
          </select>
          <input
            v-model.trim="filters.minPrice"
            class="input input-sm"
            inputmode="decimal"
            placeholder="最低单价"
            @keyup.enter="applyFilters"
          />
          <input
            v-model.trim="filters.maxPrice"
            class="input input-sm"
            inputmode="decimal"
            placeholder="最高单价"
            @keyup.enter="applyFilters"
          />
          <button class="ghost-btn" :disabled="loading || submitting" @click="applyFilters">查询</button>
          <button class="ghost-btn" :disabled="loading || submitting" @click="resetFilters">重置</button>
        </div>
      </div>

      <div class="dashboard-meta-row">
        <span>共 {{ total }} 台机位</span>
        <span>当前页 {{ page }} / {{ totalPages }} · 每页 {{ size }} 台</span>
        <span>自动刷新：15 秒</span>
        <span>统计区间：{{ statsRangeLabel }}</span>
      </div>
    </div>

    <div class="stat-grid">
      <StatCard label="空闲机位" :value="String(summary.idle)" suffix="台" :note="summary.idleNote" tone="success" />
      <StatCard label="使用中" :value="String(summary.using)" suffix="台" :note="summary.usingNote" tone="info" />
      <StatCard label="停用" :value="String(summary.disabled)" suffix="台" :note="summary.disabledNote" tone="warning" />
      <StatCard label="当前上机收入" :value="summary.revenueLabel" :note="summary.revenueNote" tone="neutral" />
      <StatCard label="今日流水" :value="summary.cashFlowLabel" :note="summary.cashFlowNote" tone="info" />
      <StatCard label="活跃用户" :value="String(summary.activeUsers)" suffix="人" :note="summary.activeUsersNote" tone="warning" />
    </div>

    <div class="dashboard-split">
      <div class="card page-card monitor-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">机位面板</p>
            <h3>运行中机位与费用动态</h3>
          </div>
          <div class="table-actions">
            <button class="ghost-btn small-btn" :disabled="loading || page <= 1" @click="changePage(page - 1)">上一页</button>
            <button class="ghost-btn small-btn" :disabled="loading || page >= totalPages" @click="changePage(page + 1)">下一页</button>
          </div>
        </div>

        <div v-if="loading" class="table-state">正在加载实时面板…</div>
        <div v-else-if="machines.length === 0" class="table-state">当前筛选条件下没有机位数据</div>
        <div v-else class="machine-grid">
          <div v-for="machine in machines" :key="machine.id" class="machine-card reveal">
            <div class="machine-meta">
              <div>
                <p class="machine-title">{{ machine.code }}</p>
                <p class="subtitle">{{ machineSpec(machine) }}</p>
              </div>
              <StatusPill :text="machine.statusLabel" :tone="machine.statusTone" />
            </div>

            <div class="machine-badges">
              <span v-if="machineZone(machine)" class="machine-badge">{{ machineZone(machine) }}</span>
              <span class="machine-badge">{{ machine.priceLabel }}</span>
            </div>

            <div class="metric-row">
              <span>当前用户</span>
              <span>{{ machine.currentUserName || '—' }}</span>
            </div>
            <div class="metric-row">
              <span>开始时间</span>
              <span>{{ formatDateTime(machine.currentStartTime) }}</span>
            </div>
            <div class="metric-row">
              <span>已上机时长</span>
              <span>{{ formatMinutes(machine.currentDurationMinutes) }}</span>
            </div>
            <div class="metric-row">
              <span>当前费用</span>
              <span>{{ formatCurrency(machine.currentFee) }}</span>
            </div>

            <div class="filter-bar action-bar">
              <button
                v-if="machine.status === 0"
                class="ghost-btn"
                :disabled="submitting"
                @click="openOpenModal(machine)"
              >
                开通上机
              </button>
              <button
                v-else-if="machine.status === 1"
                class="ghost-btn danger-btn"
                :disabled="submitting || actionLoadingId === machine.currentSessionId || !machine.currentSessionId"
                @click="forceEndMachine(machine)"
              >
                {{ actionLoadingId === machine.currentSessionId ? '处理中…' : '强制下机' }}
              </button>
              <button
                v-else
                class="ghost-btn"
                :disabled="submitting"
                @click="goMachines"
              >
                前往启用
              </button>

              <button class="ghost-btn" :disabled="submitting" @click="goSessions">
                查看上机管理
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="card side-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">运营摘要</p>
            <h3>当前营业状态</h3>
          </div>
        </div>

        <div class="record-card hero-record">
          <span>高峰时段</span>
          <strong>{{ overview?.peakHour || '—' }}</strong>
          <small>{{ overview?.sessionRevenueLabel || '等待统计数据' }}</small>
        </div>

        <div class="side-summary-grid">
          <div class="record-card">
            <span>总上机时长</span>
            <strong>{{ overview?.totalDurationLabel || '—' }}</strong>
            <small>{{ statsRangeLabel }}</small>
          </div>
          <div class="record-card">
            <span>ARPU</span>
            <strong>{{ overview?.arpuLabel || '—' }}</strong>
            <small>上机收入 / 活跃用户</small>
          </div>
        </div>

        <div class="records-section">
          <div class="section-heading">
            <h4>正在上机</h4>
            <span>{{ activeMachines.length }} 台</span>
          </div>
          <div v-if="activeMachines.length === 0" class="table-state compact-state">当前没有进行中的机位</div>
          <div v-else class="usage-list">
            <div v-for="item in activeMachines.slice(0, 5)" :key="item.id" class="usage-item">
              <div class="usage-head">
                <strong>{{ item.code }}</strong>
                <span>{{ item.currentUserName || '进行中订单' }}</span>
              </div>
              <div class="usage-meta two-lines">
                <span>{{ formatMinutes(item.currentDurationMinutes) }}</span>
                <span>{{ formatCurrency(item.currentFee) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="records-section">
          <div class="section-heading">
            <h4>空闲机位</h4>
            <span>{{ idleMachines.length }} 台</span>
          </div>
          <div v-if="idleMachines.length === 0" class="table-state compact-state">当前筛选结果里没有空闲机位</div>
          <div v-else class="idle-list">
            <button
              v-for="item in idleMachines.slice(0, 6)"
              :key="item.id"
              class="idle-chip"
              :disabled="submitting"
              @click="openOpenModal(item)"
            >
              <strong>{{ item.code }}</strong>
              <span>{{ machineZone(item) || machineSpec(item) }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showOpenModal" class="modal-overlay" @click.self="closeOpenModal">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <p class="eyebrow">快速开通</p>
            <h3>为机位创建上机订单</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="submitting" @click="closeOpenModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitOpenSession">
          <label class="field field-span-2">
            <span>目标机位</span>
            <input :value="selectedMachineLabel" class="input" disabled />
          </label>

          <label class="field field-span-2">
            <span>搜索用户</span>
            <div class="filter-bar search-row">
              <input
                v-model.trim="openForm.userKeyword"
                class="input"
                :disabled="submitting || userLookupLoading"
                placeholder="姓名 / 手机号 / 身份证"
                @keyup.enter.prevent="searchUsers"
              />
              <button type="button" class="ghost-btn" :disabled="submitting || userLookupLoading" @click="searchUsers">
                {{ userLookupLoading ? '搜索中…' : '搜索用户' }}
              </button>
            </div>
          </label>

          <div class="lookup-panel field-span-2">
            <div v-if="userLookupLoading" class="table-state compact-state">正在搜索用户…</div>
            <div v-else-if="userCandidates.length === 0" class="table-state compact-state">没有找到可用用户</div>
            <div v-else class="lookup-grid">
              <button
                v-for="user in userCandidates"
                :key="user.id"
                type="button"
                class="lookup-item"
                :class="selectedUser?.id === user.id ? 'lookup-item-active' : ''"
                :disabled="submitting"
                @click="selectUser(user)"
              >
                <strong>{{ user.name }}</strong>
                <span>{{ user.mobileMasked }}</span>
                <small>余额 {{ formatCurrency(user.balance) }}</small>
              </button>
            </div>
          </div>

          <div v-if="selectedUser" class="selected-card field-span-2">
            <span class="selected-label">已选用户</span>
            <strong>{{ selectedUser.name }}</strong>
            <small>{{ selectedUser.mobileMasked }} · 余额 {{ formatCurrency(selectedUser.balance) }}</small>
          </div>

          <p v-if="openError" class="form-error field-span-2">{{ openError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closeOpenModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting || !selectedMachine">
              {{ submitting ? '提交中…' : '确认开通上机' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppTopbar from '../../../components/AppTopbar.vue'
import StatCard from '../../../components/StatCard.vue'
import StatusPill from '../../../components/StatusPill.vue'
import { requestJson, withQuery } from '../../../access/http'
import {
  type MachineItem,
  type NoticeState,
  type PagedResponse,
  type UserLookupItem,
  formatCurrency,
  formatDate,
  formatDateTime,
  formatMinutes,
  parseMachineConfig,
  parseNumberInput,
  readErrorMessage,
  trimToUndefined
} from '../machines/shared'

type StatsOverview = {
  startDate: string
  endDate: string
  cashFlowAmount: number
  cashFlowLabel: string
  sessionRevenueAmount: number
  sessionRevenueLabel: string
  totalDurationMinutes: number
  totalDurationLabel: string
  activeUsers: number
  activeUsersLabel: string
  arpu: number
  arpuLabel: string
  peakHour: string
}

const router = useRouter()
const defaultFilters = () => ({
  keyword: '',
  status: '',
  minPrice: '',
  maxPrice: ''
})

const filters = reactive(defaultFilters())
const notice = reactive<NoticeState>({ type: 'success', message: '' })
const machines = ref<MachineItem[]>([])
const overview = ref<StatsOverview | null>(null)
const loading = ref(false)
const submitting = ref(false)
const actionLoadingId = ref<number | null>(null)
const total = ref(0)
const page = ref(1)
const size = ref(12)
let refreshTimer: number | null = null

const showOpenModal = ref(false)
const openForm = reactive({ userKeyword: '' })
const userCandidates = ref<UserLookupItem[]>([])
const userLookupLoading = ref(false)
const selectedUser = ref<UserLookupItem | null>(null)
const selectedMachine = ref<MachineItem | null>(null)
const openError = ref('')

const today = new Date()
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
const toDateInput = (value: Date) => {
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const statsRange = reactive({
  start: toDateInput(firstDay),
  end: toDateInput(today)
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const activeMachines = computed(() => machines.value.filter((item) => item.status === 1))
const idleMachines = computed(() => machines.value.filter((item) => item.status === 0))
const disabledMachines = computed(() => machines.value.filter((item) => item.status === 2))
const statsRangeLabel = computed(() => {
  if (!overview.value) {
    return `${statsRange.start} 至 ${statsRange.end}`
  }
  return `${formatDate(overview.value.startDate)} 至 ${formatDate(overview.value.endDate)}`
})
const selectedMachineLabel = computed(() => {
  if (!selectedMachine.value) {
    return '未选择机位'
  }
  const zone = machineZone(selectedMachine.value)
  const spec = machineSpec(selectedMachine.value)
  return [selectedMachine.value.code, zone, spec].filter(Boolean).join(' · ')
})

const summary = computed(() => ({
  idle: idleMachines.value.length,
  idleNote: total.value ? `占当前结果 ${(idleMachines.value.length / Math.max(total.value, 1) * 100).toFixed(0)}%` : '等待加载',
  using: activeMachines.value.length,
  usingNote: overview.value?.peakHour ? `高峰时段 ${overview.value.peakHour}` : '当前页实时统计',
  disabled: disabledMachines.value.length,
  disabledNote: disabledMachines.value.length > 0 ? '建议前往机位管理排查' : '当前页无停用机位',
  revenueLabel: overview.value?.sessionRevenueLabel ?? formatCurrency(activeMachines.value.reduce((sum, item) => sum + (Number(item.currentFee) || 0), 0)),
  revenueNote: activeMachines.value.length > 0 ? `${activeMachines.value.length} 台机位正在上机` : '当前无进行中订单',
  cashFlowLabel: overview.value?.cashFlowLabel ?? '—',
  cashFlowNote: statsRangeLabel.value,
  activeUsers: overview.value?.activeUsers ?? activeMachines.value.length,
  activeUsersNote: overview.value?.activeUsersLabel ?? '当前上机用户数',
}))

const clearNotice = () => {
  notice.message = ''
}

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const buildMachineQuery = (targetPage = page.value) => withQuery('/machines/monitor', {
  keyword: trimToUndefined(filters.keyword),
  status: filters.status,
  minPrice: parseNumberInput(filters.minPrice),
  maxPrice: parseNumberInput(filters.maxPrice),
  page: targetPage,
  size: size.value
})

const machineZone = (machine: MachineItem) => parseMachineConfig(machine.configJson).zone
const machineSpec = (machine: MachineItem) => parseMachineConfig(machine.configJson).spec || machine.configSummary || '未配置'

const fetchMachines = async (targetPage = page.value, silent = false) => {
  if (!silent) {
    loading.value = true
    clearNotice()
  }

  try {
    const data = await requestJson<PagedResponse<MachineItem>>(buildMachineQuery(targetPage))
    machines.value = data.items
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载实时监控列表失败'))
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

const fetchOverview = async (silent = false) => {
  if (!silent) {
    clearNotice()
  }
  try {
    overview.value = await requestJson<StatsOverview>(withQuery('/stats/overview', statsRange))
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载营业统计失败'))
  }
}

const refreshAll = async () => {
  loading.value = true
  clearNotice()
  try {
    await Promise.all([fetchMachines(page.value, true), fetchOverview(true)])
  } finally {
    loading.value = false
  }
}

const applyFilters = async () => {
  await fetchMachines(1)
}

const resetFilters = async () => {
  Object.assign(filters, defaultFilters())
  await fetchMachines(1)
}

const changePage = async (targetPage: number) => {
  if (targetPage < 1 || targetPage > totalPages.value || targetPage === page.value) {
    return
  }
  await fetchMachines(targetPage)
}

const searchUsers = async () => {
  userLookupLoading.value = true
  openError.value = ''
  try {
    const data = await requestJson<PagedResponse<UserLookupItem>>(
      withQuery('/users', {
        keyword: trimToUndefined(openForm.userKeyword),
        status: 1,
        page: 1,
        size: 8
      })
    )
    userCandidates.value = data.items
  } catch (error) {
    openError.value = readErrorMessage(error, '搜索用户失败')
  } finally {
    userLookupLoading.value = false
  }
}

const selectUser = (user: UserLookupItem) => {
  selectedUser.value = user
}

const openOpenModal = async (machine: MachineItem) => {
  selectedMachine.value = machine
  selectedUser.value = null
  userCandidates.value = []
  openForm.userKeyword = ''
  openError.value = ''
  showOpenModal.value = true
  await searchUsers()
}

const closeOpenModal = () => {
  if (submitting.value) {
    return
  }
  showOpenModal.value = false
  selectedMachine.value = null
  selectedUser.value = null
  userCandidates.value = []
  openForm.userKeyword = ''
  openError.value = ''
}

const submitOpenSession = async () => {
  if (!selectedMachine.value) {
    openError.value = '请选择机位'
    return
  }
  if (!selectedUser.value) {
    openError.value = '请选择用户'
    return
  }

  submitting.value = true
  clearNotice()
  try {
    await requestJson<boolean>('/sessions/open', {
      method: 'POST',
      body: JSON.stringify({
        userId: selectedUser.value.id,
        machineId: selectedMachine.value.id
      })
    })
    setNotice('success', `已为 ${selectedUser.value.name} 开通 ${selectedMachine.value.code} 上机`)
    closeOpenModal()
    await refreshAll()
  } catch (error) {
    openError.value = readErrorMessage(error, '开通上机失败')
  } finally {
    submitting.value = false
  }
}

const forceEndMachine = async (machine: MachineItem) => {
  if (!machine.currentSessionId) {
    setNotice('error', '当前机位没有可结束的会话')
    return
  }

  const userName = machine.currentUserName || '当前用户'
  if (!window.confirm(`确认强制结束 ${userName} 在 ${machine.code} 的上机订单吗？`)) {
    return
  }

  actionLoadingId.value = machine.currentSessionId
  clearNotice()
  try {
    await requestJson<boolean>(`/sessions/${machine.currentSessionId}/force-end`, {
      method: 'PUT'
    })
    setNotice('success', `已强制结束 ${machine.code} 的当前订单`)
    await refreshAll()
  } catch (error) {
    setNotice('error', readErrorMessage(error, '强制下机失败'))
  } finally {
    actionLoadingId.value = null
  }
}

const goSessions = async () => {
  await router.push('/admin/sessions')
}

const goMachines = async () => {
  await router.push('/admin/machines')
}

const startPolling = () => {
  if (refreshTimer !== null) {
    window.clearInterval(refreshTimer)
  }
  refreshTimer = window.setInterval(() => {
    void refreshAll()
  }, 15000)
}

onMounted(async () => {
  await refreshAll()
  startPolling()
})

onBeforeUnmount(() => {
  if (refreshTimer !== null) {
    window.clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.dashboard-page {
  min-height: 0;
}

.dashboard-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  color: var(--muted);
  font-size: 13px;
}

.dashboard-split {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, 0.85fr);
  gap: 16px;
}

.page-card,
.side-card {
  min-height: 0;
}

.monitor-card {
  display: flex;
  flex-direction: column;
}

.machine-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.machine-card {
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 250, 244, 0.92));
  border: 1px solid rgba(23, 23, 23, 0.05);
  box-shadow: 0 12px 30px rgba(23, 23, 23, 0.06);
  display: grid;
  gap: 12px;
}

.machine-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.machine-title {
  font-family: var(--font-mono);
  font-size: 18px;
  font-weight: 700;
  color: #27231d;
}

.machine-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.machine-badge {
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(23, 23, 23, 0.05);
  color: #6f665a;
  font-size: 12px;
}

.action-bar {
  margin-top: 4px;
}

.danger-btn {
  color: #b33838;
  border-color: rgba(224, 62, 62, 0.2);
  background: rgba(224, 62, 62, 0.06);
}

.hero-record {
  margin-bottom: 14px;
}

.side-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.records-section {
  margin-top: 16px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-heading h4 {
  margin: 0;
  font-size: 16px;
}

.section-heading span {
  color: var(--muted);
  font-size: 13px;
}

.usage-list {
  display: grid;
  gap: 10px;
}

.usage-item {
  padding: 14px;
  border-radius: 16px;
  background: rgba(23, 23, 23, 0.03);
  border: 1px solid rgba(23, 23, 23, 0.05);
}

.usage-head,
.usage-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.usage-head strong {
  font-family: var(--font-mono);
  font-size: 14px;
}

.two-lines {
  color: var(--muted);
  font-size: 13px;
}

.idle-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.idle-chip {
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid rgba(29, 154, 163, 0.12);
  background: rgba(29, 154, 163, 0.06);
  text-align: left;
  display: grid;
  gap: 4px;
  cursor: pointer;
}

.idle-chip strong {
  font-family: var(--font-mono);
  color: #1d6f76;
}

.idle-chip span {
  color: #5f6d70;
  font-size: 12px;
}

.search-row {
  width: 100%;
}

.lookup-panel {
  border: 1px solid rgba(23, 23, 23, 0.06);
  border-radius: 18px;
  background: rgba(23, 23, 23, 0.02);
  padding: 14px;
}

.lookup-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.lookup-item {
  text-align: left;
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid rgba(23, 23, 23, 0.08);
  background: rgba(255, 255, 255, 0.9);
  display: grid;
  gap: 4px;
  cursor: pointer;
}

.lookup-item strong {
  color: #27231d;
}

.lookup-item span,
.lookup-item small {
  color: var(--muted);
}

.lookup-item-active {
  border-color: rgba(29, 111, 209, 0.35);
  background: rgba(29, 111, 209, 0.08);
}

.selected-card {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(29, 154, 163, 0.08);
  border: 1px solid rgba(29, 154, 163, 0.15);
  display: grid;
  gap: 4px;
}

.selected-label {
  color: #0e6f76;
  font-size: 12px;
}

.compact-state {
  padding: 8px 0;
  min-height: 0;
}

.input-sm {
  max-width: 120px;
}

@media (max-width: 1400px) {
  .dashboard-split {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .machine-grid,
  .lookup-grid,
  .idle-list,
  .side-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
