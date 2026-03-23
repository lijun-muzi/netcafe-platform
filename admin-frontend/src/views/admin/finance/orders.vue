<template>
  <section class="page">
    <AppTopbar title="订单与充值" subtitle="流水与充值记录" eyebrow="财务" />

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">订单列表</p>
          <h3>上机订单</h3>
        </div>
        <div class="filter-bar">
          <input class="input" placeholder="用户/机位" />
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
            <th>开始/结束</th>
            <th>时长</th>
            <th>单价</th>
            <th>金额</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in orders" :key="row.id">
            <td>{{ row.user }}</td>
            <td>{{ row.machine }}</td>
            <td>{{ row.time }}</td>
            <td>{{ row.duration }}</td>
            <td>{{ row.price }}</td>
            <td>{{ row.amount }}</td>
            <td><StatusPill :text="row.status" :tone="row.tone" /></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">充值记录</p>
          <h3>线下充值</h3>
        </div>
        <div class="filter-bar">
          <select class="select">
            <option>全部渠道</option>
            <option>现金</option>
            <option>其他</option>
          </select>
          <input class="input" placeholder="时间范围" />
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>用户</th>
            <th>金额</th>
            <th>渠道</th>
            <th>经办人</th>
            <th>时间</th>
            <th>备注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in recharges" :key="row.id">
            <td>{{ row.user }}</td>
            <td>{{ row.amount }}</td>
            <td>{{ row.channel }}</td>
            <td>{{ row.operator }}</td>
            <td>{{ row.time }}</td>
            <td>{{ row.remark }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import AppTopbar from '../../../components/AppTopbar.vue'
import StatusPill from '../../../components/StatusPill.vue'

const orders = [
  { id: 1, user: '张伟', machine: 'A-021', time: '10:12 - 12:05', duration: '113 分钟', price: '¥0.12', amount: '¥13.56', status: '完成', tone: 'idle' },
  { id: 2, user: '林涛', machine: 'A-011', time: '15:05 - 15:38', duration: '33 分钟', price: '¥0.12', amount: '¥3.96', status: '强制结束', tone: 'forced' }
]

const recharges = [
  { id: 1, user: '刘阳', amount: '¥100', channel: '现金', operator: '王杰', time: '今日 09:30', remark: '加时' },
  { id: 2, user: '孙雨', amount: '¥50', channel: '其他', operator: '陈璐', time: '昨日 19:20', remark: '活动补贴' }
]
</script>
