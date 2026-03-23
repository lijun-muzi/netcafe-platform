<template>
  <section class="page">
    <AppTopbar title="用户管理" subtitle="注册与余额状态" eyebrow="用户" />

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>手机号 / 身份证 / 状态</h3>
        </div>
        <div class="filter-bar">
          <input class="input" placeholder="姓名/手机号/身份证" />
          <select class="select">
            <option>全部状态</option>
            <option>可用</option>
            <option>冻结</option>
          </select>
          <button class="solid-btn">新增用户</button>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>手机号</th>
            <th>身份证</th>
            <th>余额</th>
            <th>状态</th>
            <th>最近上机</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in users" :key="row.phone">
            <td>{{ row.name }}</td>
            <td>{{ row.phone }}</td>
            <td>{{ row.idCard }}</td>
            <td>{{ row.balance }}</td>
            <td><StatusPill :text="row.status" :tone="row.tone" /></td>
            <td>{{ row.last }}</td>
            <td>
              <div class="filter-bar">
                <button class="ghost-btn">编辑</button>
                <button class="ghost-btn">充值</button>
                <button class="ghost-btn">冻结</button>
                <button class="ghost-btn">记录</button>
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

const users = [
  { name: '张伟', phone: '138****2311', idCard: '320****1996', balance: '¥86.40', status: '可用', tone: 'idle', last: '今日 10:12' },
  { name: '刘阳', phone: '186****7820', idCard: '110****2001', balance: '¥22.10', status: '可用', tone: 'idle', last: '今日 09:45' },
  { name: '孙雨', phone: '155****4421', idCard: '330****1999', balance: '¥3.20', status: '冻结', tone: 'disabled', last: '2026-01-12' }
]
</script>
