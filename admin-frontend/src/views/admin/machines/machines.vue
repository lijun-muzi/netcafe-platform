<template>
  <section class="page">
    <AppTopbar title="机位管理" subtitle="配置、单价与维护状态" eyebrow="设备" />

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>编号 / 状态 / 单价区间</h3>
        </div>
        <div class="filter-bar">
          <input class="input" placeholder="机位编号" />
          <select class="select">
            <option>全部状态</option>
            <option>空闲</option>
            <option>使用中</option>
            <option>停用</option>
          </select>
          <button class="ghost-btn">模板批量建机位</button>
          <button class="solid-btn">新增机位</button>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>编号</th>
            <th>状态</th>
            <th>单价</th>
            <th>配置摘要</th>
            <th>创建时间</th>
            <th>维护时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in machines" :key="row.code">
            <td>{{ row.code }}</td>
            <td><StatusPill :text="row.status" :tone="row.tone" /></td>
            <td>{{ row.price }}</td>
            <td>{{ row.config }}</td>
            <td>{{ row.created }}</td>
            <td>{{ row.maintained }}</td>
            <td>
              <div class="filter-bar">
                <button class="ghost-btn">编辑</button>
                <button class="ghost-btn">调价</button>
                <button class="ghost-btn">停用</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import AppTopbar from '../../../components/AppTopbar.vue'
import StatusPill from '../../../components/StatusPill.vue'

const machines = [
  {
    code: 'A-021',
    status: '使用中',
    tone: 'using',
    price: '¥0.12/分钟',
    config: 'i7 / RTX 4070 / 32G',
    created: '2025-12-08',
    maintained: '2026-01-18'
  },
  {
    code: 'A-022',
    status: '空闲',
    tone: 'idle',
    price: '¥0.10/分钟',
    config: 'i5 / RTX 3060 / 16G',
    created: '2025-11-21',
    maintained: '2026-01-10'
  },
  {
    code: 'C-003',
    status: '停用',
    tone: 'disabled',
    price: '¥0.08/分钟',
    config: 'i5 / GTX 1660 / 16G',
    created: '2025-10-02',
    maintained: '2026-01-06'
  }
]
</script>
