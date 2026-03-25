<template>
  <section class="page">
    <AppTopbar title="实时面板" subtitle="机位运行状态与费用即时概览" eyebrow="首页" />

    <div class="stat-grid">
      <StatCard
        v-for="(card, index) in stats"
        :key="card.label"
        class="reveal"
        :style="{ animationDelay: `${index * 0.05}s` }"
        :label="card.label"
        :value="card.value"
        :suffix="card.suffix"
        :note="card.note"
        :tone="card.tone"
      />
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">机位筛选</p>
          <h3>状态与配置过滤</h3>
        </div>
        <div class="filter-bar">
          <input v-model.trim="filters.keyword" class="input" placeholder="编号搜索" />
          <select v-model="filters.status" class="select">
            <option value="">全部状态</option>
            <option value="空闲">空闲</option>
            <option value="使用中">使用中</option>
            <option value="停用">停用</option>
          </select>
          <select v-model="filters.priceRange" class="select">
            <option value="">单价区间</option>
            <option value="0.08-0.12">0.08 - 0.12</option>
            <option value="0.12-0.18">0.12 - 0.18</option>
          </select>
          <button class="ghost-btn" @click="resetFilters">重置</button>
        </div>
      </div>

      <div class="machine-grid">
        <div v-for="machine in filteredMachines" :key="machine.code" class="machine-card reveal">
          <div class="machine-meta">
            <div>
              <p class="machine-title">{{ machine.code }}</p>
              <p class="subtitle">{{ machine.config }}</p>
            </div>
            <StatusPill :text="machine.statusText" :tone="machine.tone" />
          </div>
          <div class="metric-row">
            <span>当前用户</span>
            <span>{{ machine.user }}</span>
          </div>
          <div class="metric-row">
            <span>开始时间</span>
            <span>{{ machine.start }}</span>
          </div>
          <div class="metric-row">
            <span>当前费用</span>
            <span>{{ machine.fee }}</span>
          </div>
          <div class="filter-bar">
            <button class="ghost-btn">开通上机</button>
            <button class="ghost-btn">强制下机</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import AppTopbar from '../../../components/AppTopbar.vue'
import StatCard from '../../../components/StatCard.vue'
import StatusPill from '../../../components/StatusPill.vue'

const stats = [
  { label: '空闲机位', value: '68', suffix: '台', note: '较昨日 +4', tone: 'success' },
  { label: '使用中', value: '32', suffix: '台', note: '高峰运行', tone: 'info' },
  { label: '停用', value: '6', suffix: '台', note: '维护中', tone: 'warning' },
  { label: '今日流水', value: '¥3,284', suffix: '', note: '预计夜间峰值', tone: 'neutral' }
]

const defaultFilters = () => ({
  keyword: '',
  status: '',
  priceRange: ''
})

const filters = reactive(defaultFilters())

const machineSource = [
  {
    code: 'A-021',
    config: 'i7 / RTX 4070 / 32G',
    statusText: '使用中',
    tone: 'using',
    user: '张伟',
    start: '10:12',
    fee: '¥12.8',
    pricePerMin: 0.12
  },
  {
    code: 'A-022',
    config: 'i5 / RTX 3060 / 16G',
    statusText: '空闲',
    tone: 'idle',
    user: '-',
    start: '-',
    fee: '¥0.0',
    pricePerMin: 0.1
  },
  {
    code: 'B-014',
    config: 'R7 / RTX 4060 / 32G',
    statusText: '使用中',
    tone: 'using',
    user: '刘阳',
    start: '09:45',
    fee: '¥18.4',
    pricePerMin: 0.1
  },
  {
    code: 'C-003',
    config: 'i5 / GTX 1660 / 16G',
    statusText: '停用',
    tone: 'disabled',
    user: '-',
    start: '-',
    fee: '¥0.0',
    pricePerMin: 0.08
  }
]

const filteredMachines = computed(() =>
  machineSource.filter((machine) => {
    const keywordMatched = !filters.keyword || machine.code.toLowerCase().includes(filters.keyword.toLowerCase())
    const statusMatched = !filters.status || machine.statusText === filters.status
    const priceMatched =
      !filters.priceRange ||
      (filters.priceRange === '0.08-0.12'
        ? machine.pricePerMin >= 0.08 && machine.pricePerMin <= 0.12
        : machine.pricePerMin > 0.12 && machine.pricePerMin <= 0.18)
    return keywordMatched && statusMatched && priceMatched
  })
)

const resetFilters = () => {
  Object.assign(filters, defaultFilters())
}
</script>
