<template>
  <section class="page">
    <AppTopbar title="上机管理" subtitle="进行中与历史订单" eyebrow="上机" />

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">进行中</p>
          <h3>实时计费订单</h3>
        </div>
        <div class="filter-bar">
          <input class="input" placeholder="用户/机位" />
          <button class="solid-btn">开通上机</button>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>用户</th>
            <th>机位</th>
            <th>开始时间</th>
            <th>已计费分钟</th>
            <th>当前费用</th>
            <th>余额</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in ongoing" :key="row.user">
            <td>{{ row.user }}</td>
            <td>{{ row.machine }}</td>
            <td>{{ row.start }}</td>
            <td>{{ row.minutes }}</td>
            <td>{{ row.fee }}</td>
            <td>{{ row.balance }}</td>
            <td><button class="ghost-btn">强制下机</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">历史</p>
          <h3>已完成订单</h3>
        </div>
        <div class="filter-bar">
          <input class="input" placeholder="时间范围" />
          <select class="select">
            <option>全部状态</option>
            <option>完成</option>
            <option>强制结束</option>
          </select>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>用户</th>
            <th>机位</th>
            <th>开始</th>
            <th>结束</th>
            <th>时长</th>
            <th>金额</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in history" :key="row.id">
            <td>{{ row.user }}</td>
            <td>{{ row.machine }}</td>
            <td>{{ row.start }}</td>
            <td>{{ row.end }}</td>
            <td>{{ row.duration }}</td>
            <td>{{ row.amount }}</td>
            <td><StatusPill :text="row.status" :tone="row.tone" /></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import AppTopbar from '../../../components/AppTopbar.vue'
import StatusPill from '../../../components/StatusPill.vue'

const ongoing = [
  { user: '张伟', machine: 'A-021', start: '10:12', minutes: '86', fee: '¥10.32', balance: '¥86.40' },
  { user: '刘阳', machine: 'B-014', start: '09:45', minutes: '92', fee: '¥11.04', balance: '¥22.10' }
]

const history = [
  { id: 1, user: '孙雨', machine: 'C-003', start: '昨天 18:22', end: '昨天 20:01', duration: '99 分钟', amount: '¥9.90', status: '完成', tone: 'idle' },
  { id: 2, user: '林涛', machine: 'A-011', start: '昨天 15:05', end: '昨天 15:38', duration: '33 分钟', amount: '¥3.96', status: '强制结束', tone: 'forced' }
]
</script>
