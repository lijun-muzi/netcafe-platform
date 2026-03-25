<template>
  <section class="page stats-page">
    <AppTopbar
      title="运营统计"
      subtitle="区间总览、趋势分析与运营榜单"
      eyebrow="统计"
      action-label="刷新列表"
      :action-disabled="loading"
      :action-handler="refreshAll"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>统计区间与榜单设置</h3>
        </div>
        <div class="filter-bar">
          <input v-model="filters.start" class="input input-date" type="date" />
          <input v-model="filters.end" class="input input-date" type="date" />
          <select v-model="filters.granularity" class="select">
            <option value="day">按日趋势</option>
            <option value="month">按月趋势</option>
          </select>
          <input v-model="filters.limit" class="input input-short" type="number" min="1" max="20" placeholder="榜单条数" />
          <input
            v-model="filters.threshold"
            class="input input-short"
            type="number"
            min="0"
            placeholder="低余额阈值(分钟)"
          />
          <button class="ghost-btn" :disabled="loading" @click="applyFilters">查询</button>
          <button class="ghost-btn" :disabled="loading" @click="resetFilters">重置</button>
        </div>
      </div>

      <div class="stats-filter-summary">
        <span>统计范围：{{ dateRangeLabel }}</span>
        <span>趋势颗粒度：{{ filters.granularity === 'month' ? '按月' : '按日' }}</span>
        <span>榜单条数：TOP {{ normalizedLimit }}</span>
        <span>低余额阈值：{{ thresholdLabel }}</span>
      </div>
    </div>

    <div class="stat-grid">
      <StatCard label="充值流水" :value="overview?.cashFlowLabel ?? '—'" :note="periodNote" tone="info" />
      <StatCard label="上机收入" :value="overview?.sessionRevenueLabel ?? '—'" :note="peakHourNote" tone="warning" />
      <StatCard label="总上机时长" :value="String(overview?.totalDurationMinutes ?? 0)" suffix="分钟" :note="durationNote" tone="success" />
      <StatCard label="活跃用户" :value="String(overview?.activeUsers ?? 0)" suffix="人" :note="activeUserNote" tone="neutral" />
      <StatCard label="ARPU" :value="overview?.arpuLabel ?? '—'" :note="arpuNote" tone="info" />
      <StatCard label="高峰时段" :value="overview?.peakHour ?? '—'" :note="summaryNote" tone="warning" />
    </div>

    <div class="stats-split">
      <div class="card stats-chart-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">趋势</p>
            <h3>流水与时长走势</h3>
          </div>
          <div class="stats-legend">
            <span><i class="legend-dot legend-cash"></i>充值流水</span>
            <span><i class="legend-dot legend-revenue"></i>上机收入</span>
          </div>
        </div>

        <div v-if="loading" class="table-state">正在加载趋势数据…</div>
        <div v-else-if="trendBars.length === 0" class="table-state">当前区间没有趋势数据</div>
        <div v-else class="trend-layout">
          <div class="trend-chart">
            <div v-for="point in trendBars" :key="point.label" class="trend-column">
              <div class="trend-tooltip">
                <strong>{{ point.label }}</strong>
                <span>充值流水：{{ point.cashFlowLabel }}</span>
                <span>上机收入：{{ point.sessionRevenueLabel }}</span>
                <span>上机时长：{{ point.durationLabel }}</span>
                <span>活跃用户：{{ point.activeUsersLabel }}</span>
              </div>
              <div class="trend-bars">
                <span class="trend-bar trend-bar-cash" :style="{ height: `${point.cashHeight}%` }"></span>
                <span class="trend-bar trend-bar-revenue" :style="{ height: `${point.revenueHeight}%` }"></span>
              </div>
              <strong>{{ point.label }}</strong>
              <small>{{ point.activeUsersLabel }}</small>
            </div>
          </div>

          <div class="mini-table-wrap trend-detail-wrap">
            <table class="table compact-table">
              <thead>
                <tr>
                  <th>区间</th>
                  <th>充值流水</th>
                  <th>上机收入</th>
                  <th>上机时长</th>
                  <th>活跃用户</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="point in trend?.points ?? []" :key="point.label">
                  <td>{{ point.label }}</td>
                  <td>{{ point.cashFlowLabel }}</td>
                  <td>{{ point.sessionRevenueLabel }}</td>
                  <td>{{ point.durationLabel }}</td>
                  <td>{{ point.activeUsersLabel }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="card stats-side-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">机位使用率</p>
            <h3>热门机位排行</h3>
          </div>
        </div>

        <div v-if="loading" class="table-state">正在加载机位排行…</div>
        <div v-else-if="machineUsage.length === 0" class="table-state">当前区间没有机位使用数据</div>
        <div v-else class="usage-list">
          <div v-for="item in machineUsage" :key="item.machineId" class="usage-item">
            <div class="usage-head">
              <strong>{{ item.machineCode }}</strong>
              <span>{{ item.usageRateLabel }}</span>
            </div>
            <div class="usage-track">
              <span class="usage-fill" :style="{ width: item.usageRatePercent }"></span>
            </div>
            <div class="usage-meta">
              <span>{{ item.usageMinutesLabel }}</span>
              <span>{{ item.revenueLabel }}</span>
            </div>
          </div>
        </div>

        <div class="side-summary-grid">
          <div class="record-card">
            <span>机位收入 TOP</span>
            <strong>{{ rankings?.machineRevenueTop[0]?.machineCode ?? '—' }}</strong>
            <small>{{ rankings?.machineRevenueTop[0]?.revenueLabel ?? '暂无数据' }}</small>
          </div>
          <div class="record-card">
            <span>用户消费 TOP</span>
            <strong>{{ rankings?.userConsumeTop[0]?.userName ?? '—' }}</strong>
            <small>{{ rankings?.userConsumeTop[0]?.totalAmountLabel ?? '暂无数据' }}</small>
          </div>
        </div>

        <div class="records-section">
          <div class="section-heading">
            <h4>长时间闲置机位</h4>
          </div>
          <div class="mini-table-wrap">
            <table class="table compact-table">
              <thead>
                <tr>
                  <th>机位</th>
                  <th>闲置时长</th>
                  <th>闲置率</th>
                </tr>
              </thead>
              <tbody v-if="idleMachines.length">
                <tr v-for="item in idleMachines" :key="item.machineId">
                  <td>{{ item.machineCode }}</td>
                  <td>{{ item.idleMinutesLabel }}</td>
                  <td>{{ item.idleRateLabel }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="!loading && idleMachines.length === 0" class="table-state">当前区间没有闲置机位数据</div>
          </div>
        </div>
      </div>
    </div>

    <div class="stats-ranking-grid">
      <div class="card ranking-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">榜单</p>
            <h3>机位收入 TOP</h3>
          </div>
        </div>

        <div class="mini-table-wrap ranking-table-wrap">
          <table class="table compact-table">
            <thead>
              <tr>
                <th>机位</th>
                <th>收入</th>
                <th>订单数</th>
              </tr>
            </thead>
            <tbody v-if="rankings?.machineRevenueTop.length">
              <tr v-for="item in rankings.machineRevenueTop" :key="item.machineId">
                <td>{{ item.machineCode }}</td>
                <td>{{ item.revenueLabel }}</td>
                <td>{{ item.sessionCount }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="!loading && !rankings?.machineRevenueTop.length" class="table-state">当前区间没有机位收入数据</div>
        </div>
      </div>

      <div class="card ranking-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">榜单</p>
            <h3>用户消费 TOP</h3>
          </div>
        </div>

        <div class="mini-table-wrap ranking-table-wrap">
          <table class="table compact-table">
            <thead>
              <tr>
                <th>用户</th>
                <th>手机号</th>
                <th>消费</th>
                <th>时长</th>
              </tr>
            </thead>
            <tbody v-if="rankings?.userConsumeTop.length">
              <tr v-for="item in rankings.userConsumeTop" :key="item.userId">
                <td>{{ item.userName }}</td>
                <td>{{ item.mobileMasked }}</td>
                <td>{{ item.totalAmountLabel }}</td>
                <td>{{ item.totalDurationLabel }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="!loading && !rankings?.userConsumeTop.length" class="table-state">当前区间没有用户消费数据</div>
        </div>
      </div>

      <div class="card ranking-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">榜单</p>
            <h3>低余额用户</h3>
          </div>
        </div>

        <div class="mini-table-wrap ranking-table-wrap">
          <table class="table compact-table">
            <thead>
              <tr>
                <th>用户</th>
                <th>手机号</th>
                <th>余额</th>
                <th>剩余时长</th>
              </tr>
            </thead>
            <tbody v-if="rankings?.lowBalanceUsers.length">
              <tr v-for="item in rankings.lowBalanceUsers" :key="item.userId">
                <td>{{ item.userName }}</td>
                <td>{{ item.mobileMasked }}</td>
                <td>{{ item.balanceLabel }}</td>
                <td>{{ item.remainingMinutesLabel }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="!loading && !rankings?.lowBalanceUsers.length" class="table-state">当前没有低余额用户</div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppTopbar from '../../../components/AppTopbar.vue'
import StatCard from '../../../components/StatCard.vue'
import { requestJson, withQuery } from '../../../access/http'
import { type NoticeState, formatDate, readErrorMessage } from '../shared'

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
type StatsTrendPoint = {
  label: string
  cashFlowAmount: number
  cashFlowLabel: string
  sessionRevenueAmount: number
  sessionRevenueLabel: string
  durationMinutes: number
  durationLabel: string
  activeUsers: number
  activeUsersLabel: string
}
type StatsTrend = {
  granularity: 'day' | 'month'
  startDate: string
  endDate: string
  points: StatsTrendPoint[]
}
type StatsMachineUsage = {
  machineId: number
  machineCode: string
  usageMinutes: number
  usageMinutesLabel: string
  usageRate: number
  usageRateLabel: string
  revenueAmount: number
  revenueLabel: string
}
type StatsMachineTop = {
  machineId: number
  machineCode: string
  revenueAmount: number
  revenueLabel: string
  sessionCount: number
}
type StatsIdleMachine = {
  machineId: number
  machineCode: string
  usageMinutes: number
  usageMinutesLabel: string
  idleMinutes: number
  idleMinutesLabel: string
  idleRate: number
  idleRateLabel: string
}
type StatsUserTop = {
  userId: number
  userName: string
  mobileMasked: string
  totalAmount: number
  totalAmountLabel: string
  totalDurationMinutes: number
  totalDurationLabel: string
}
type StatsLowBalanceUser = {
  userId: number
  userName: string
  mobileMasked: string
  balance: number
  balanceLabel: string
  remainingMinutes: number
  remainingMinutesLabel: string
}
type StatsRankings = {
  machineRevenueTop: StatsMachineTop[]
  userConsumeTop: StatsUserTop[]
  lowBalanceUsers: StatsLowBalanceUser[]
}
type TrendBar = {
  label: string
  cashHeight: number
  revenueHeight: number
  activeUsersLabel: string
  cashFlowLabel: string
  sessionRevenueLabel: string
  durationLabel: string
}
const today = new Date()
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
const toDateInput = (value: Date) => {
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
const defaultFilters = () => ({
  start: toDateInput(firstDay),
  end: toDateInput(today),
  granularity: 'day' as 'day' | 'month',
  limit: '5',
  threshold: ''
})
const filters = reactive(defaultFilters())
const loading = ref(false)
const notice = ref<NoticeState>({ type: 'success', message: '' })
const overview = ref<StatsOverview | null>(null)
const trend = ref<StatsTrend | null>(null)
const machineUsage = ref<(StatsMachineUsage & { usageRatePercent: string })[]>([])
const idleMachines = ref<StatsIdleMachine[]>([])
const rankings = ref<StatsRankings | null>(null)
const normalizedLimit = computed(() => {
  const value = Number(filters.limit)
  if (!Number.isFinite(value) || value <= 0) {
    return 5
  }
  return Math.min(20, Math.floor(value))
})
const normalizedThreshold = computed(() => {
  const value = Number(filters.threshold)
  if (!Number.isFinite(value) || value < 0) {
    return undefined
  }
  return Math.floor(value)
})
const dateRangeLabel = computed(() => {
  if (!overview.value) {
    return `${filters.start} 至 ${filters.end}`
  }
  return `${formatDate(overview.value.startDate)} 至 ${formatDate(overview.value.endDate)}`
})
const thresholdLabel = computed(() => {
  if (normalizedThreshold.value === undefined) {
    return '系统默认值'
  }
  return `${normalizedThreshold.value} 分钟`
})
const periodNote = computed(() => (overview.value ? `${dateRangeLabel.value}` : '等待加载'))
const peakHourNote = computed(() => (overview.value?.peakHour ? `高峰 ${overview.value.peakHour}` : '等待加载'))
const durationNote = computed(() => (overview.value?.sessionRevenueLabel ? `收入 ${overview.value.sessionRevenueLabel}` : '等待加载'))
const activeUserNote = computed(() => (overview.value?.activeUsersLabel ? `${overview.value.activeUsersLabel} 内有充值或上机` : '等待加载'))
const arpuNote = computed(() => (overview.value?.arpuLabel ? '上机收入 / 活跃用户' : '等待加载'))
const summaryNote = computed(() => (trend.value?.granularity === 'month' ? '按月汇总走势' : '按日汇总走势'))
const trendBars = computed<TrendBar[]>(() => {
  const points = trend.value?.points ?? []
  if (points.length === 0) {
    return []
  }

  const maxCash = Math.max(...points.map((item) => Number(item.cashFlowAmount) || 0), 1)
  const maxRevenue = Math.max(...points.map((item) => Number(item.sessionRevenueAmount) || 0), 1)

  return points.map((item) => ({
    label: item.label,
    cashHeight: Math.max(10, Math.round(((Number(item.cashFlowAmount) || 0) / maxCash) * 100)),
    revenueHeight: Math.max(10, Math.round(((Number(item.sessionRevenueAmount) || 0) / maxRevenue) * 100)),
    activeUsersLabel: item.activeUsersLabel,
    cashFlowLabel: item.cashFlowLabel,
    sessionRevenueLabel: item.sessionRevenueLabel,
    durationLabel: item.durationLabel
  }))
})
const buildBaseQuery = () => ({
  start: filters.start,
  end: filters.end
})
const validateFilters = () => {
  if (filters.start && filters.end && filters.start > filters.end) {
    notice.value = {
      type: 'error',
      message: '开始日期不能晚于结束日期'
    }
    return false
  }

  if (filters.start && filters.end) {
    const startDate = new Date(`${filters.start}T00:00:00`)
    const endDate = new Date(`${filters.end}T00:00:00`)
    const rangeDays = Math.floor((endDate.getTime() - startDate.getTime()) / 86400000) + 1
    if (rangeDays > 366) {
      notice.value = {
        type: 'error',
        message: '统计区间不能超过 366 天'
      }
      return false
    }
  }

  return true
}
const fetchOverview = async () => {
  overview.value = await requestJson<StatsOverview>(withQuery('/stats/overview', buildBaseQuery()))
}
const fetchTrend = async () => {
  trend.value = await requestJson<StatsTrend>(
    withQuery('/stats/trend', {
      ...buildBaseQuery(),
      granularity: filters.granularity
    })
  )
}
const fetchMachineUsage = async () => {
  const data = await requestJson<StatsMachineUsage[]>(
    withQuery('/stats/machine-usage', {
      ...buildBaseQuery(),
      limit: normalizedLimit.value
    })
  )
  machineUsage.value = data.map((item) => ({
    ...item,
    usageRatePercent: `${Math.min(100, Math.max(0, Number(item.usageRate) * 100))}%`
  }))
}
const fetchIdleMachines = async () => {
  idleMachines.value = await requestJson<StatsIdleMachine[]>(
    withQuery('/stats/idle-machines', {
      ...buildBaseQuery(),
      limit: normalizedLimit.value
    })
  )
}
const fetchRankings = async () => {
  rankings.value = await requestJson<StatsRankings>(
    withQuery('/stats/rankings', {
      ...buildBaseQuery(),
      limit: normalizedLimit.value,
      threshold: normalizedThreshold.value
    })
  )
}
const refreshAll = async () => {
  if (!validateFilters()) {
    return
  }

  loading.value = true
  notice.value = { type: 'success', message: '' }

  try {
    await Promise.all([fetchOverview(), fetchTrend(), fetchMachineUsage(), fetchIdleMachines(), fetchRankings()])
  } catch (error) {
    notice.value = {
      type: 'error',
      message: readErrorMessage(error, '运营统计数据加载失败')
    }
  } finally {
    loading.value = false
  }
}
const applyFilters = async () => {
  await refreshAll()
}
const resetFilters = async () => {
  Object.assign(filters, defaultFilters())
  await refreshAll()
}
onMounted(async () => {
  await refreshAll()
})
</script>

<style scoped>
.stats-page {
  min-height: 0;
}

.stats-filter-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  color: var(--muted);
  font-size: 13px;
}

.stats-split {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, 0.95fr);
  gap: 16px;
}

.stats-chart-card,
.stats-side-card,
.ranking-card {
  min-height: 0;
}

.stats-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: var(--muted);
  font-size: 13px;
}

.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-right: 6px;
  border-radius: 999px;
}

.legend-cash {
  background: rgba(255, 138, 0, 0.9);
}

.legend-revenue {
  background: rgba(29, 154, 163, 0.9);
}
.trend-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: 16px;
  align-items: stretch;
}
.trend-chart {
  min-height: 260px;
  padding: 18px 18px 14px;
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 138, 0, 0.12), transparent 40%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.8), rgba(255, 249, 241, 0.86));
  border: 1px solid rgba(255, 138, 0, 0.08);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(58px, 1fr));
  gap: 12px;
  align-items: end;
}
.trend-column {
  min-width: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.trend-tooltip {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 8px);
  transform: translateX(-50%);
  min-width: 132px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(23, 23, 23, 0.94);
  color: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 34px rgba(23, 23, 23, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: grid;
  gap: 4px;
  z-index: 12;
  pointer-events: none;
  opacity: 0;
  visibility: hidden;
  transition:
    opacity 160ms ease,
    visibility 160ms ease,
    transform 160ms ease;
  white-space: nowrap;
}
.trend-column:hover .trend-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(-4px);
}
.trend-tooltip strong {
  font-size: 12px;
  letter-spacing: 0.04em;
}
.trend-tooltip span {
  font-size: 12px;
  line-height: 1.45;
}
.trend-column strong {
  font-family: var(--font-mono);
  font-size: 13px;
}
.trend-column small {
  color: var(--muted);
  font-size: 12px;
}
.trend-bars {
  width: 100%;
  height: 176px;
  display: flex;
  align-items: end;
  justify-content: center;
  gap: 6px;
}
.trend-bar {
  width: calc(50% - 4px);
  min-height: 10px;
  border-radius: 10px 10px 4px 4px;
}
.trend-bar-cash {
  background: linear-gradient(180deg, rgba(255, 138, 0, 0.95), rgba(255, 194, 116, 0.7));
}
.trend-bar-revenue {
  background: linear-gradient(180deg, rgba(29, 154, 163, 0.95), rgba(98, 203, 211, 0.72));
}
.trend-detail-wrap,
.ranking-table-wrap {
  height: 100%;
}
.compact-table th,
.compact-table td {
  padding: 10px 8px;
}
.usage-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
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
  font-size: 15px;
}
.usage-head span,
.usage-meta {
  color: var(--muted);
  font-size: 13px;
}
.usage-track {
  height: 10px;
  margin: 12px 0 10px;
  border-radius: 999px;
  background: rgba(23, 23, 23, 0.08);
  overflow: hidden;
}
.usage-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(29, 111, 209, 0.92), rgba(29, 154, 163, 0.85));
}
.side-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}
.stats-ranking-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.ranking-card {
  display: flex;
  flex-direction: column;
}

@media (max-width: 1400px) {
  .stats-split {
    grid-template-columns: 1fr;
  }

  .trend-layout {
    grid-template-columns: 1fr;
  }

  .stats-ranking-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .side-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
