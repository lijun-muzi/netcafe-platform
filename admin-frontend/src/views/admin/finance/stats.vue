<template>
  <section class="page">
    <AppTopbar title="运营统计" subtitle="月度与自定义区间" eyebrow="统计" />

    <div class="stat-grid">
      <StatCard label="流水" value="¥128,420" note="本月 +12%" tone="info" />
      <StatCard label="总上机时长" value="9,284" suffix="分钟" note="峰值 19:00" tone="success" />
      <StatCard label="活跃用户" value="324" suffix="人" note="较上周 +18" tone="neutral" />
      <StatCard label="ARPU" value="¥396" note="运营健康" tone="warning" />
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">趋势</p>
          <h3>流水与时长走势</h3>
        </div>
        <div class="filter-bar">
          <select class="select">
            <option>按日</option>
            <option>按月</option>
          </select>
          <input class="input" placeholder="选择时间范围" />
        </div>
      </div>
      <div class="chart">
        <div class="chart-bars">
          <span style="height: 40%"></span>
          <span style="height: 65%"></span>
          <span style="height: 55%"></span>
          <span style="height: 80%"></span>
          <span style="height: 60%"></span>
          <span style="height: 85%"></span>
          <span style="height: 72%"></span>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">榜单</p>
          <h3>机位与用户排行</h3>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>机位收入 TOP</th>
            <th>收入</th>
            <th>用户消费 TOP</th>
            <th>消费金额</th>
            <th>低余额用户</th>
            <th>余额</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>A-021</td>
            <td>¥8,240</td>
            <td>张伟</td>
            <td>¥1,240</td>
            <td>孙雨</td>
            <td>¥3.20</td>
          </tr>
          <tr>
            <td>B-014</td>
            <td>¥7,980</td>
            <td>刘阳</td>
            <td>¥980</td>
            <td>林涛</td>
            <td>¥8.60</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import AppTopbar from '../../../components/AppTopbar.vue'
import StatCard from '../../../components/StatCard.vue'
</script>
