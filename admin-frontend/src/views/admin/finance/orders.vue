<template>
  <section class="page">
    <AppTopbar
      title="订单与充值"
      subtitle="上机流水与线下充值记录"
      eyebrow="财务"
      action-label="刷新列表"
      :action-disabled="loadingOrders || loadingRecharges || submittingRecharge"
      :action-handler="refreshAll"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="finance-grid">
      <div class="card page-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">订单列表</p>
            <h3>上机订单</h3>
          </div>
          <div class="filter-bar">
            <input
              v-model.trim="orderFilters.keyword"
              class="input"
              placeholder="用户姓名 / 手机号 / 身份证 / 机位"
              @keyup.enter="applyOrderFilters"
            />
            <select v-model="orderFilters.status" class="select">
              <option value="">全部状态</option>
              <option value="1">完成</option>
              <option value="2">强制结束</option>
            </select>
            <button class="ghost-btn" :disabled="loadingOrders" @click="applyOrderFilters">查询</button>
            <button class="ghost-btn" :disabled="loadingOrders" @click="resetOrderFilters">重置</button>
          </div>
        </div>

        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>用户</th>
                <th>机位</th>
                <th>开始 / 结束</th>
                <th>时长</th>
                <th>单价</th>
                <th>金额</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody v-if="orders.length > 0">
              <tr v-for="row in orders" :key="row.id">
                <td>{{ row.userName || `用户 #${row.userId}` }}</td>
                <td>{{ row.machineCode || `机位 #${row.machineId}` }}</td>
                <td>
                  <div class="time-cell">
                    <strong>{{ row.timeRangeLabel || '—' }}</strong>
                    <span>开始 {{ formatDateTime(row.startTime) }}</span>
                    <span>结束 {{ formatDateTime(row.endTime) }}</span>
                  </div>
                </td>
                <td>{{ row.durationLabel || formatMinutes(row.durationMinutes) }}</td>
                <td>{{ row.priceLabel || formatCurrency(row.priceSnapshot) }}</td>
                <td>{{ row.amountLabel || formatCurrency(row.amount) }}</td>
                <td>
                  <StatusPill :text="row.statusLabel" :tone="row.statusTone" />
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="loadingOrders" class="table-state">正在加载订单列表…</div>
          <div v-else-if="orders.length === 0" class="table-state">当前筛选条件下没有订单数据</div>
        </div>

        <div class="pagination-bar">
          <p class="pagination-text">共 {{ orderTotal }} 条，当前第 {{ orderPage }} / {{ orderTotalPages }} 页</p>
          <div class="table-actions">
            <button
              class="ghost-btn small-btn"
              :disabled="loadingOrders || orderPage <= 1"
              @click="changeOrderPage(orderPage - 1)"
            >
              上一页
            </button>
            <button
              class="ghost-btn small-btn"
              :disabled="loadingOrders || orderPage >= orderTotalPages"
              @click="changeOrderPage(orderPage + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>

      <div class="card page-card">
        <div class="card-header">
          <div>
            <p class="eyebrow">充值记录</p>
            <h3>线下充值</h3>
          </div>
          <div class="filter-bar">
            <select v-model="rechargeFilters.channel" class="select">
              <option value="">全部渠道</option>
              <option v-for="option in rechargeChannelOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
            <input v-model="rechargeFilters.dateFrom" class="input input-date" type="date" />
            <input v-model="rechargeFilters.dateTo" class="input input-date" type="date" />
            <button class="ghost-btn" :disabled="loadingRecharges" @click="applyRechargeFilters">查询</button>
            <button class="ghost-btn" :disabled="loadingRecharges" @click="resetRechargeFilters">重置</button>
            <button class="solid-btn" :disabled="submittingRecharge" @click="openRechargeModal">新增充值</button>
          </div>
        </div>

        <div class="table-wrap">
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
            <tbody v-if="recharges.length > 0">
              <tr v-for="row in recharges" :key="row.id">
                <td>{{ row.userName || `用户 #${row.userId}` }}</td>
                <td>{{ row.amountLabel || formatCurrency(row.amount) }}</td>
                <td>{{ row.channel }}</td>
                <td>{{ row.operatorName || (row.operatorAdminId ? `管理员 #${row.operatorAdminId}` : '—') }}</td>
                <td>{{ formatDateTime(row.createdAt) }}</td>
                <td class="remark-cell">{{ row.remark || '—' }}</td>
              </tr>
            </tbody>
          </table>

          <div v-if="loadingRecharges" class="table-state">正在加载充值记录…</div>
          <div v-else-if="recharges.length === 0" class="table-state">当前筛选条件下没有充值记录</div>
        </div>

        <div class="pagination-bar">
          <p class="pagination-text">
            共 {{ rechargeTotal }} 条，当前第 {{ rechargePage }} / {{ rechargeTotalPages }} 页
          </p>
          <div class="table-actions">
            <button
              class="ghost-btn small-btn"
              :disabled="loadingRecharges || rechargePage <= 1"
              @click="changeRechargePage(rechargePage - 1)"
            >
              上一页
            </button>
            <button
              class="ghost-btn small-btn"
              :disabled="loadingRecharges || rechargePage >= rechargeTotalPages"
              @click="changeRechargePage(rechargePage + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showRechargeModal" class="modal-overlay" @click.self="closeRechargeModal">
      <div class="modal-card modal-card-xl">
        <div class="modal-header">
          <div>
            <p class="eyebrow">充值</p>
            <h3>手工充值</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="submittingRecharge" @click="closeRechargeModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitRecharge">
          <div class="field field-span-2 selection-panel">
            <div class="selection-header">
              <div>
                <span>选择充值用户</span>
                <small>可按姓名、手机号或身份证搜索任意用户</small>
              </div>
              <div class="filter-bar compact-bar">
                <input
                  v-model.trim="rechargeLookupKeyword"
                  class="input"
                  placeholder="姓名 / 手机号 / 身份证"
                  @keyup.enter.prevent="searchRechargeUsers()"
                />
                <button
                  class="ghost-btn small-btn"
                  type="button"
                  :disabled="loadingRechargeUsers"
                  @click="searchRechargeUsers()"
                >
                  搜索用户
                </button>
              </div>
            </div>

            <div class="selection-grid">
              <button
                v-for="user in rechargeCandidates"
                :key="user.id"
                type="button"
                class="selection-item"
                :class="selectedRechargeUser?.id === user.id ? 'selection-item-active' : ''"
                @click="selectRechargeUser(user)"
              >
                <strong>{{ user.name }}</strong>
                <span>{{ user.mobileMasked }}</span>
                <span>余额 {{ formatCurrency(user.balance) }}</span>
              </button>
              <div v-if="loadingRechargeUsers" class="table-state">正在搜索用户…</div>
              <div v-else-if="rechargeCandidates.length === 0" class="table-state">没有找到可充值的用户</div>
            </div>
          </div>

          <div class="field field-span-2 selected-strip">
            <div class="selected-card">
              <span class="selected-label">已选用户</span>
              <strong>{{ selectedRechargeUser ? selectedRechargeUser.name : '未选择' }}</strong>
              <small v-if="selectedRechargeUser">
                {{ selectedRechargeUser.mobileMasked }} · 当前余额 {{ formatCurrency(selectedRechargeUser.balance) }}
              </small>
            </div>
          </div>

          <label class="field">
            <span>充值金额</span>
            <input
              v-model.trim="rechargeForm.amount"
              class="input"
              type="number"
              min="0.01"
              step="0.01"
              :disabled="submittingRecharge"
              placeholder="请输入充值金额"
            />
          </label>

          <label class="field">
            <span>充值渠道</span>
            <select v-model="rechargeForm.channel" class="select" :disabled="submittingRecharge">
              <option value="">请选择渠道</option>
              <option v-for="option in rechargeChannelOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label class="field field-span-2">
            <span>备注</span>
            <input
              v-model.trim="rechargeForm.remark"
              class="input"
              :disabled="submittingRecharge"
              placeholder="例如：前台现金充值、活动赠送"
            />
          </label>

          <p v-if="rechargeError" class="form-error field-span-2">{{ rechargeError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submittingRecharge" @click="closeRechargeModal">
              取消
            </button>
            <button class="solid-btn" type="submit" :disabled="submittingRecharge">
              {{ submittingRecharge ? '提交中…' : '确认充值' }}
            </button>
          </div>
        </form>
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
  formatCurrency,
  formatDateTime,
  formatMinutes,
  readErrorMessage,
  trimToUndefined
} from '../shared'

type OrderItem = {
  id: number
  userId: number
  userName: string | null
  machineId: number
  machineCode: string | null
  startTime: string | null
  endTime: string | null
  timeRangeLabel: string
  durationMinutes: number | null
  durationLabel: string
  priceSnapshot: number | null
  priceLabel: string
  amount: number | null
  amountLabel: string
  status: 1 | 2
  statusLabel: string
  statusTone: 'idle' | 'forced'
  forceByAdminId: number | null
}

type RechargeItem = {
  id: number
  userId: number
  userName: string | null
  amount: number
  amountLabel: string
  channel: string
  operatorAdminId: number | null
  operatorName: string | null
  createdAt: string | null
  remark: string | null
}

type RechargeUserCandidate = {
  id: number
  name: string
  mobileMasked: string
  balance: number
  status: 0 | 1
  statusLabel: string
}

const defaultOrderFilters = () => ({
  keyword: '',
  status: ''
})
const defaultRechargeFilters = () => ({
  channel: '',
  dateFrom: '',
  dateTo: ''
})
const orderFilters = reactive(defaultOrderFilters())
const rechargeFilters = reactive(defaultRechargeFilters())
const rechargeForm = reactive({
  amount: '',
  channel: '',
  remark: ''
})

const orders = ref<OrderItem[]>([])
const recharges = ref<RechargeItem[]>([])
const rechargeChannelOptions = ref<SelectOption[]>([])
const rechargeCandidates = ref<RechargeUserCandidate[]>([])
const selectedRechargeUser = ref<RechargeUserCandidate | null>(null)
const rechargeLookupKeyword = ref('')

const orderTotal = ref(0)
const orderPage = ref(1)
const orderSize = ref(10)
const rechargeTotal = ref(0)
const rechargePage = ref(1)
const rechargeSize = ref(10)

const loadingOrders = ref(false)
const loadingRecharges = ref(false)
const loadingRechargeUsers = ref(false)
const submittingRecharge = ref(false)
const showRechargeModal = ref(false)
const rechargeError = ref('')
const notice = reactive<NoticeState>({ type: 'success', message: '' })

const orderTotalPages = computed(() => Math.max(1, Math.ceil(orderTotal.value / orderSize.value)))
const rechargeTotalPages = computed(() => Math.max(1, Math.ceil(rechargeTotal.value / rechargeSize.value)))

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const clearNotice = () => {
  notice.message = ''
}

const fetchOrders = async (targetPage = orderPage.value) => {
  loadingOrders.value = true
  clearNotice()
  try {
    const data = await requestJson<PagedResponse<OrderItem>>(
      withQuery('/session-orders', {
        keyword: trimToUndefined(orderFilters.keyword),
        status: orderFilters.status,
        page: targetPage,
        size: orderSize.value
      })
    )
    orders.value = data.items
    orderTotal.value = data.total
    orderPage.value = data.page
    orderSize.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载订单列表失败'))
  } finally {
    loadingOrders.value = false
  }
}

const fetchRecharges = async (targetPage = rechargePage.value) => {
  loadingRecharges.value = true
  clearNotice()
  try {
    const data = await requestJson<PagedResponse<RechargeItem>>(
      withQuery('/recharges', {
        channel: rechargeFilters.channel,
        dateFrom: rechargeFilters.dateFrom,
        dateTo: rechargeFilters.dateTo,
        page: targetPage,
        size: rechargeSize.value
      })
    )
    recharges.value = data.items
    rechargeTotal.value = data.total
    rechargePage.value = data.page
    rechargeSize.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载充值记录失败'))
  } finally {
    loadingRecharges.value = false
  }
}

const fetchRechargeChannels = async () => {
  try {
    rechargeChannelOptions.value = await requestJson<SelectOption[]>('/recharges/channel-options')
    if (!rechargeForm.channel && rechargeChannelOptions.value.length > 0) {
      rechargeForm.channel = rechargeChannelOptions.value[0].value
    }
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载充值渠道失败'))
  }
}

const refreshAll = () => {
  void Promise.all([fetchOrders(orderPage.value), fetchRecharges(rechargePage.value)])
}

const applyOrderFilters = () => {
  void fetchOrders(1)
}

const resetOrderFilters = () => {
  Object.assign(orderFilters, defaultOrderFilters())
  void fetchOrders(1)
}

const changeOrderPage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > orderTotalPages.value || targetPage === orderPage.value) {
    return
  }
  void fetchOrders(targetPage)
}

const applyRechargeFilters = () => {
  void fetchRecharges(1)
}

const resetRechargeFilters = () => {
  Object.assign(rechargeFilters, defaultRechargeFilters())
  void fetchRecharges(1)
}

const changeRechargePage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > rechargeTotalPages.value || targetPage === rechargePage.value) {
    return
  }
  void fetchRecharges(targetPage)
}

const resetRechargeModalState = () => {
  rechargeLookupKeyword.value = ''
  selectedRechargeUser.value = null
  rechargeCandidates.value = []
  rechargeForm.amount = ''
  rechargeForm.channel = rechargeChannelOptions.value[0]?.value ?? ''
  rechargeForm.remark = ''
  rechargeError.value = ''
}

const searchRechargeUsers = async () => {
  loadingRechargeUsers.value = true
  rechargeError.value = ''
  try {
    const data = await requestJson<PagedResponse<RechargeUserCandidate>>(
      withQuery('/users', {
        keyword: trimToUndefined(rechargeLookupKeyword.value),
        page: 1,
        size: 8
      })
    )
    rechargeCandidates.value = data.items
  } catch (error) {
    rechargeError.value = readErrorMessage(error, '搜索充值用户失败')
  } finally {
    loadingRechargeUsers.value = false
  }
}

const selectRechargeUser = (user: RechargeUserCandidate) => {
  selectedRechargeUser.value = user
}

const openRechargeModal = async () => {
  resetRechargeModalState()
  showRechargeModal.value = true
  if (rechargeChannelOptions.value.length === 0) {
    await fetchRechargeChannels()
  }
  await searchRechargeUsers()
}

const closeRechargeModal = () => {
  if (submittingRecharge.value) {
    return
  }
  showRechargeModal.value = false
  rechargeError.value = ''
}

const validateRechargeForm = () => {
  if (!selectedRechargeUser.value) {
    return '请选择充值用户'
  }
  const amount = Number(rechargeForm.amount)
  if (!Number.isFinite(amount) || amount < 0.01) {
    return '请输入不小于 0.01 的充值金额'
  }
  if (!rechargeForm.channel) {
    return '请选择充值渠道'
  }
  return ''
}

const submitRecharge = async () => {
  rechargeError.value = validateRechargeForm()
  if (rechargeError.value || !selectedRechargeUser.value) {
    return
  }

  submittingRecharge.value = true
  clearNotice()
  try {
    await requestJson<boolean>('/recharges', {
      method: 'POST',
      body: JSON.stringify({
        userId: selectedRechargeUser.value.id,
        amount: Number(rechargeForm.amount),
        channel: rechargeForm.channel,
        remark: trimToUndefined(rechargeForm.remark)
      })
    })
    showRechargeModal.value = false
    setNotice('success', `已为 ${selectedRechargeUser.value.name} 完成线下充值`)
    await fetchRecharges(1)
  } catch (error) {
    rechargeError.value = readErrorMessage(error, '充值失败')
  } finally {
    submittingRecharge.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchRechargeChannels(), fetchOrders(1), fetchRecharges(1)])
})
</script>

<style scoped>
.page {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.finance-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-rows: minmax(0, 1fr) minmax(0, 1fr);
  gap: 20px;
}

.page-card {
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.table-wrap {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.time-cell {
  display: grid;
  gap: 4px;
}

.time-cell strong {
  color: #30281f;
  font-size: 13px;
}

.time-cell span {
  color: #7d7568;
  font-size: 12px;
}

.remark-cell {
  max-width: 240px;
  color: #5f564a;
  white-space: normal;
  line-height: 1.45;
}

.modal-card-xl {
  width: min(980px, calc(100vw - 32px));
}

.selection-panel {
  display: grid;
  gap: 12px;
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.selection-header span {
  display: block;
  color: #30281f;
  font-weight: 700;
}

.selection-header small {
  display: block;
  margin-top: 4px;
  color: #7d7568;
  font-size: 12px;
}

.selection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.selection-item {
  display: grid;
  gap: 6px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.78);
  text-align: left;
  transition: border-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.selection-item:hover {
  border-color: rgba(255, 138, 0, 0.24);
  transform: translateY(-1px);
}

.selection-item strong {
  color: #27231d;
  font-size: 15px;
}

.selection-item span {
  color: #7d7568;
  font-size: 12px;
}

.selection-item-active {
  border-color: rgba(255, 138, 0, 0.45);
  box-shadow: 0 10px 24px rgba(255, 138, 0, 0.12);
  background: rgba(255, 249, 241, 0.96);
}

.selected-strip {
  display: grid;
}

.selected-card {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: var(--bg-alt);
  display: grid;
  gap: 6px;
}

.selected-label {
  color: #7d7568;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.selected-card strong {
  font-size: 18px;
  color: #27231d;
}

.selected-card small {
  color: #7d7568;
  font-size: 12px;
}

.compact-bar {
  gap: 8px;
}

.input-date {
  max-width: 150px;
}

@media (max-width: 1200px) {
  .selection-header {
    flex-direction: column;
  }
}

@media (max-width: 960px) {
  .finance-grid {
    grid-template-rows: repeat(2, minmax(320px, 1fr));
  }
}
</style>
