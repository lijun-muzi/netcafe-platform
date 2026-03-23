<template>
  <section class="page">
    <AppTopbar title="审计日志" subtitle="关键操作留痕" eyebrow="审计" />

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>操作人 / 类型 / 时间范围</h3>
        </div>
        <div class="filter-bar">
          <input class="input" placeholder="操作人" />
          <select class="select">
            <option>全部类型</option>
            <option>调价</option>
            <option>强制下机</option>
            <option>充值</option>
          </select>
          <input class="input" placeholder="时间范围" />
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>操作人</th>
            <th>角色</th>
            <th>操作类型</th>
            <th>对象</th>
            <th>时间</th>
            <th>前后值</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in logs" :key="row.id">
            <td>{{ row.operator }}</td>
            <td>{{ row.role }}</td>
            <td>{{ row.action }}</td>
            <td>{{ row.target }}</td>
            <td>{{ row.time }}</td>
            <td>{{ row.diff }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import AppTopbar from '../../../components/AppTopbar.vue'

const logs = [
  { id: 1, operator: '李青', role: '超级管理员', action: '调价', target: 'A-021', time: '今日 09:12', diff: '0.10 → 0.12' },
  { id: 2, operator: '王杰', role: '管理员', action: '强制下机', target: '用户 张伟', time: '今日 10:30', diff: '余额不足' },
  { id: 3, operator: '陈璐', role: '管理员', action: '充值', target: '用户 刘阳', time: '昨日 19:20', diff: '+¥50 现金' }
]
</script>
