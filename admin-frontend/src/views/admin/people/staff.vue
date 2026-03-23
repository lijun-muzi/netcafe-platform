<template>
  <section class="page">
    <AppTopbar title="员工管理" subtitle="仅超级管理员可见" eyebrow="人员" />

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>账号与角色</h3>
        </div>
        <div class="filter-bar">
          <input class="input" placeholder="账号/姓名" />
          <select class="select">
            <option>全部角色</option>
            <option>超级管理员</option>
            <option>管理员</option>
          </select>
          <select class="select">
            <option>全部状态</option>
            <option>启用</option>
            <option>禁用</option>
          </select>
          <button class="solid-btn">新增员工</button>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>账号</th>
            <th>姓名</th>
            <th>角色</th>
            <th>状态</th>
            <th>最近登录</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in staff" :key="row.account">
            <td>{{ row.account }}</td>
            <td>{{ row.name }}</td>
            <td>{{ row.role }}</td>
            <td><StatusPill :text="row.status" :tone="row.tone" /></td>
            <td>{{ row.last }}</td>
            <td>
              <div class="filter-bar">
                <button class="ghost-btn">编辑</button>
                <button class="ghost-btn">重置密码</button>
                <button class="ghost-btn">禁用</button>
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

const staff = [
  { account: 'superadmin', name: '李青', role: '超级管理员', status: '启用', tone: 'idle', last: '今日 09:02' },
  { account: 'admin_01', name: '王杰', role: '管理员', status: '启用', tone: 'idle', last: '昨日 22:40' },
  { account: 'admin_02', name: '陈璐', role: '管理员', status: '禁用', tone: 'disabled', last: '2026-01-11' }
]
</script>
