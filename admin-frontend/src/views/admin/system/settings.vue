<template>
  <section class="page">
    <AppTopbar title="系统设置" subtitle="计费与模板配置" eyebrow="设置" />

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">基础参数</p>
          <h3>默认单价与提醒阈值</h3>
        </div>
        <button class="solid-btn">保存配置</button>
      </div>

      <div class="filter-bar">
        <label>
          默认单价
          <input class="input" value="0.10 元/分钟" />
        </label>
        <label>
          余额提醒阈值
          <input class="input" value="60 分钟" />
        </label>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">机位模板</p>
          <h3>常用配置模板</h3>
        </div>
        <button class="solid-btn">新增模板</button>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>模板名称</th>
            <th>配置摘要</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in templates" :key="row.name">
            <td>{{ row.name }}</td>
            <td>{{ row.config }}</td>
            <td>{{ row.updated }}</td>
            <td>
              <div class="filter-bar">
                <button class="ghost-btn">编辑</button>
                <button class="ghost-btn">删除</button>
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

const templates = [
  { name: '电竞旗舰', config: 'i7 / RTX 4070 / 32G / 2K屏', updated: '2026-01-20' },
  { name: '大众畅玩', config: 'i5 / RTX 3060 / 16G / 144Hz', updated: '2026-01-18' },
  { name: '训练入门', config: 'i3 / GTX 1660 / 16G / 1080p', updated: '2026-01-05' }
]
</script>

<style scoped>
label {
  font-size: 13px;
  color: var(--muted);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>
