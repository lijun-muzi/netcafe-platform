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
          <input class="input" placeholder="编号搜索" />
          <select class="select">
            <option>全部状态</option>
            <option>空闲</option>
            <option>使用中</option>
            <option>停用</option>
          </select>
          <select class="select">
            <option>单价区间</option>
            <option>0.08 - 0.12</option>
            <option>0.12 - 0.18</option>
          </select>
          <button class="ghost-btn">重置</button>
        </div>
      </div>

      <div class="machine-grid">
        <div v-for="machine in machines" :key="machine.code" class="machine-card reveal">
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
import AppTopbar from '../../../components/AppTopbar.vue'
import StatCard from '../../../components/StatCard.vue'
import StatusPill from '../../../components/StatusPill.vue'

const stats = [
  { label: '空闲机位', value: '68', suffix: '台', note: '较昨日 +4', tone: 'success' },
  { label: '使用中', value: '32', suffix: '台', note: '高峰运行', tone: 'info' },
  { label: '停用', value: '6', suffix: '台', note: '维护中', tone: 'warning' },
  { label: '今日流水', value: '¥3,284', suffix: '', note: '预计夜间峰值', tone: 'neutral' }
]

const machines = [
  {
    code: 'A-021',
    config: 'i7 / RTX 4070 / 32G',
    statusText: '使用中',
    tone: 'using',
    user: '张伟',
    start: '10:12',
    fee: '¥12.8'
  },
  {
    code: 'A-022',
    config: 'i5 / RTX 3060 / 16G',
    statusText: '空闲',
    tone: 'idle',
    user: '-',
    start: '-',
    fee: '¥0.0'
  },
  {
    code: 'B-014',
    config: 'R7 / RTX 4060 / 32G',
    statusText: '使用中',
    tone: 'using',
    user: '刘阳',
    start: '09:45',
    fee: '¥18.4'
  },
  {
    code: 'C-003',
    config: 'i5 / GTX 1660 / 16G',
    statusText: '停用',
    tone: 'disabled',
    user: '-',
    start: '-',
    fee: '¥0.0'
  }
]
</script>
