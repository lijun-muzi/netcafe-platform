<template>
  <section class="page">
    <AppTopbar
      title="审计日志"
      subtitle="关键操作留痕与前后值对照"
      eyebrow="审计"
      action-label="刷新列表"
      :action-disabled="loading"
      :action-handler="refreshLogs"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card page-card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>操作人 / 类型 / 时间范围</h3>
        </div>
        <div class="filter-bar">
          <input
            v-model.trim="filters.operatorKeyword"
            class="input"
            placeholder="操作人姓名"
            @keyup.enter="applyFilters"
          />
          <select v-model="filters.action" class="select">
            <option value="">全部类型</option>
            <option v-for="option in actionOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <input v-model="filters.dateFrom" class="input input-date" type="date" />
          <input v-model="filters.dateTo" class="input input-date" type="date" />
          <button class="ghost-btn" :disabled="loading" @click="applyFilters">查询</button>
          <button class="ghost-btn" :disabled="loading" @click="resetFilters">重置</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>操作人</th>
              <th>角色</th>
              <th>操作类型</th>
              <th>对象</th>
              <th>变更摘要</th>
              <th>时间</th>
              <th>详情</th>
            </tr>
          </thead>
          <tbody v-if="logs.length > 0">
            <tr v-for="row in logs" :key="row.id">
              <td>{{ row.operatorName || `管理员 #${row.operatorId}` }}</td>
              <td>
                <StatusPill :text="row.operatorRoleLabel || row.operatorRole" :tone="row.operatorRole === 'SUPER_ADMIN' ? 'info' : 'idle'" />
              </td>
              <td>{{ row.actionLabel || row.action }}</td>
              <td>{{ row.targetLabel || `${row.targetType} #${row.targetId}` }}</td>
              <td class="summary-cell">{{ row.changeSummary || '—' }}</td>
              <td>{{ formatDateTime(row.createdAt) }}</td>
              <td>
                <button class="ghost-btn small-btn" @click="openDetailModal(row)">查看详情</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loading" class="table-state">正在加载审计日志…</div>
        <div v-else-if="logs.length === 0" class="table-state">当前筛选条件下没有审计日志</div>
      </div>

      <div class="pagination-bar">
        <p class="pagination-text">共 {{ total }} 条，当前第 {{ page }} / {{ totalPages }} 页</p>
        <div class="table-actions">
          <button class="ghost-btn small-btn" :disabled="loading || page <= 1" @click="changePage(page - 1)">上一页</button>
          <button class="ghost-btn small-btn" :disabled="loading || page >= totalPages" @click="changePage(page + 1)">下一页</button>
        </div>
      </div>
    </div>

    <div v-if="detailLog" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-card modal-card-wide">
        <div class="modal-header">
          <div>
            <p class="eyebrow">日志详情</p>
            <h3>{{ detailLog.actionLabel || detailLog.action }}</h3>
          </div>
          <button class="ghost-btn small-btn" @click="closeDetailModal">关闭</button>
        </div>

        <div class="record-grid">
          <div class="record-card">
            <p class="eyebrow">操作人</p>
            <strong>{{ detailLog.operatorName || `管理员 #${detailLog.operatorId}` }}</strong>
          </div>
          <div class="record-card">
            <p class="eyebrow">角色</p>
            <strong>{{ detailLog.operatorRoleLabel || detailLog.operatorRole }}</strong>
          </div>
          <div class="record-card">
            <p class="eyebrow">操作对象</p>
            <strong>{{ detailLog.targetLabel || `${detailLog.targetType} #${detailLog.targetId}` }}</strong>
          </div>
          <div class="record-card">
            <p class="eyebrow">时间</p>
            <strong>{{ formatDateTime(detailLog.createdAt) }}</strong>
          </div>
        </div>

        <div class="detail-section">
          <p class="eyebrow">变更摘要</p>
          <div class="detail-summary">{{ detailLog.changeSummary || '—' }}</div>
        </div>

        <div class="diff-grid">
          <section class="diff-panel">
            <div class="diff-header">
              <p class="eyebrow">变更前</p>
              <strong>Before</strong>
            </div>
            <pre>{{ detailBeforeJson }}</pre>
          </section>

          <section class="diff-panel">
            <div class="diff-header">
              <p class="eyebrow">变更后</p>
              <strong>After</strong>
            </div>
            <pre>{{ detailAfterJson }}</pre>
          </section>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppTopbar from '../../../components/AppTopbar.vue'
import StatusPill from '../../../components/StatusPill.vue'
import { requestJson, withQuery } from '../../../access/http'
import {
  type NoticeState,
  type PagedResponse,
  type SelectOption,
  formatDateTime,
  prettyJson,
  readErrorMessage
} from '../shared'

type AuditLogItem = {
  id: number
  operatorId: number
  operatorName: string | null
  operatorRole: string
  operatorRoleLabel: string
  action: string
  actionLabel: string
  targetType: string
  targetId: number
  targetLabel: string
  changeSummary: string
  beforeData: string | null
  afterData: string | null
  createdAt: string | null
}

const defaultFilters = () => ({
  operatorKeyword: '',
  action: '',
  dateFrom: '',
  dateTo: ''
})
const filters = reactive(defaultFilters())
const logs = ref<AuditLogItem[]>([])
const actionOptions = ref<SelectOption[]>([])
const detailLog = ref<AuditLogItem | null>(null)
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(12)
const notice = reactive<NoticeState>({ type: 'success', message: '' })

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const detailBeforeJson = computed(() => prettyJson(detailLog.value?.beforeData))
const detailAfterJson = computed(() => prettyJson(detailLog.value?.afterData))

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const clearNotice = () => {
  notice.message = ''
}

const fetchActionOptions = async () => {
  try {
    actionOptions.value = await requestJson<SelectOption[]>('/audit/logs/action-options')
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载操作类型失败'))
  }
}

const fetchLogs = async (targetPage = page.value) => {
  loading.value = true
  clearNotice()
  try {
    const data = await requestJson<PagedResponse<AuditLogItem>>(
      withQuery('/audit/logs', {
        operatorKeyword: filters.operatorKeyword,
        action: filters.action,
        dateFrom: filters.dateFrom,
        dateTo: filters.dateTo,
        page: targetPage,
        size: size.value
      })
    )
    logs.value = data.items
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载审计日志失败'))
  } finally {
    loading.value = false
  }
}

const refreshLogs = () => {
  void fetchLogs(page.value)
}

const applyFilters = () => {
  void fetchLogs(1)
}

const resetFilters = () => {
  Object.assign(filters, defaultFilters())
  void fetchLogs(1)
}

const changePage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > totalPages.value || targetPage === page.value) {
    return
  }
  void fetchLogs(targetPage)
}

const openDetailModal = (row: AuditLogItem) => {
  detailLog.value = row
}

const closeDetailModal = () => {
  detailLog.value = null
}

onMounted(async () => {
  await Promise.all([fetchActionOptions(), fetchLogs(1)])
})
</script>

<style scoped>
.page {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.page-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.table-wrap {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.summary-cell {
  max-width: 320px;
  color: #5f564a;
  line-height: 1.45;
  white-space: normal;
}

.input-date {
  max-width: 150px;
}

.detail-section {
  margin-top: 18px;
  display: grid;
  gap: 8px;
}

.detail-summary {
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--bg-alt);
  border: 1px solid var(--border);
  color: #41392e;
  line-height: 1.6;
}

.diff-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.diff-panel {
  min-height: 0;
  display: grid;
  gap: 10px;
}

.diff-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.diff-header strong {
  color: #27231d;
  font-size: 15px;
}

.diff-panel pre {
  min-height: 240px;
  max-height: 360px;
  overflow: auto;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: rgba(23, 23, 23, 0.04);
  color: #4f473b;
  font-size: 12px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 960px) {
  .diff-grid {
    grid-template-columns: 1fr;
  }
}
</style>
