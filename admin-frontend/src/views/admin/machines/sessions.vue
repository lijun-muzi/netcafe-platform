<template>
  <section class="page">
    <AppTopbar
      title="上机管理"
      subtitle="进行中与历史订单"
      eyebrow="上机"
      action-label="刷新列表"
      :action-disabled="loadingCurrent || loadingHistory"
      :action-handler="refreshAll"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card page-card">
      <div class="card-header">
        <div>
          <p class="eyebrow">进行中</p>
          <h3>实时计费订单</h3>
        </div>
        <div class="filter-bar">
          <input
            v-model.trim="currentFilters.keyword"
            class="input"
            placeholder="用户/手机号/身份证/机位"
            @keyup.enter="applyCurrentFilters"
          />
          <button class="ghost-btn" :disabled="loadingCurrent" @click="applyCurrentFilters">查询</button>
          <button class="ghost-btn" :disabled="loadingCurrent" @click="resetCurrentFilters">重置</button>
          <button class="solid-btn" :disabled="submitting" @click="openOpenModal">开通上机</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>用户</th>
              <th>手机号</th>
              <th>机位</th>
              <th>开始时间</th>
              <th>已计费</th>
              <th>当前费用</th>
              <th>余额</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="currentSessions.length > 0">
            <tr v-for="row in currentSessions" :key="row.id">
              <td>{{ row.userName || `用户 #${row.userId}` }}</td>
              <td>{{ row.userMobile || '—' }}</td>
              <td>{{ row.machineCode || `机位 #${row.machineId}` }}</td>
              <td>{{ formatDateTime(row.startTime) }}</td>
              <td>{{ formatMinutes(row.durationMinutes ?? row.billedMinutes) }}</td>
              <td>{{ formatCurrency(row.currentFee) }}</td>
              <td>{{ formatCurrency(row.userBalance) }}</td>
              <td>
                <button
                  class="ghost-btn small-btn danger-btn"
                  :disabled="actionLoadingId === row.id"
                  @click="forceEnd(row)"
                >
                  强制下机
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loadingCurrent" class="table-state">正在加载进行中订单…</div>
        <div v-else-if="currentSessions.length === 0" class="table-state">当前没有进行中的上机订单</div>
      </div>

      <div class="pagination-bar">
        <p class="pagination-text">共 {{ currentTotal }} 条，当前第 {{ currentPage }} / {{ currentTotalPages }} 页</p>
        <div class="table-actions">
          <button
            class="ghost-btn small-btn"
            :disabled="loadingCurrent || currentPage <= 1"
            @click="changeCurrentPage(currentPage - 1)"
          >
            上一页
          </button>
          <button
            class="ghost-btn small-btn"
            :disabled="loadingCurrent || currentPage >= currentTotalPages"
            @click="changeCurrentPage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <div class="card page-card">
      <div class="card-header">
        <div>
          <p class="eyebrow">历史</p>
          <h3>已完成订单</h3>
        </div>
        <div class="filter-bar">
          <input
            v-model.trim="historyFilters.keyword"
            class="input"
            placeholder="用户/手机号/身份证/机位"
            @keyup.enter="applyHistoryFilters"
          />
          <select v-model="historyFilters.status" class="select">
            <option value="">全部状态</option>
            <option value="1">完成</option>
            <option value="2">强制结束</option>
          </select>
          <input v-model="historyFilters.dateFrom" class="input input-date" type="date" />
          <input v-model="historyFilters.dateTo" class="input input-date" type="date" />
          <button class="ghost-btn" :disabled="loadingHistory" @click="applyHistoryFilters">查询</button>
          <button class="ghost-btn" :disabled="loadingHistory" @click="resetHistoryFilters">重置</button>
        </div>
      </div>

      <div class="table-wrap">
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
          <tbody v-if="historySessions.length > 0">
            <tr v-for="row in historySessions" :key="row.id">
              <td>{{ row.userName || `用户 #${row.userId}` }}</td>
              <td>{{ row.machineCode || `机位 #${row.machineId}` }}</td>
              <td>{{ formatDateTime(row.startTime) }}</td>
              <td>{{ formatDateTime(row.endTime) }}</td>
              <td>{{ formatMinutes(row.durationMinutes) }}</td>
              <td>{{ formatCurrency(row.amount) }}</td>
              <td>
                <StatusPill :text="row.statusLabel" :tone="row.statusTone" />
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loadingHistory" class="table-state">正在加载历史订单…</div>
        <div v-else-if="historySessions.length === 0" class="table-state">当前筛选条件下没有历史订单</div>
      </div>

      <div class="pagination-bar">
        <p class="pagination-text">共 {{ historyTotal }} 条，当前第 {{ historyPage }} / {{ historyTotalPages }} 页</p>
        <div class="table-actions">
          <button
            class="ghost-btn small-btn"
            :disabled="loadingHistory || historyPage <= 1"
            @click="changeHistoryPage(historyPage - 1)"
          >
            上一页
          </button>
          <button
            class="ghost-btn small-btn"
            :disabled="loadingHistory || historyPage >= historyTotalPages"
            @click="changeHistoryPage(historyPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <div v-if="showOpenModal" class="modal-overlay" @click.self="closeOpenModal">
      <div class="modal-card modal-card-xl">
        <div class="modal-header">
          <div>
            <p class="eyebrow">开通</p>
            <h3>开通上机</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="submitting" @click="closeOpenModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitOpenSession">
          <div class="field field-span-2 selection-panel">
            <div class="selection-header">
              <div>
                <span>选择用户</span>
                <small>仅可用用户可开通上机</small>
              </div>
              <div class="filter-bar compact-bar">
                <input
                  v-model.trim="openForm.userKeyword"
                  class="input"
                  placeholder="姓名/手机号/身份证"
                  @keyup.enter.prevent="searchUsers()"
                />
                <button class="ghost-btn small-btn" type="button" :disabled="userLookupLoading" @click="searchUsers()">
                  搜索用户
                </button>
              </div>
            </div>

            <div class="selection-grid">
              <button
                v-for="user in userCandidates"
                :key="user.id"
                type="button"
                class="selection-item"
                :class="selectedUser?.id === user.id ? 'selection-item-active' : ''"
                @click="selectUser(user)"
              >
                <strong>{{ user.name }}</strong>
                <span>{{ user.mobileMasked }}</span>
                <span>余额 {{ formatCurrency(user.balance) }}</span>
              </button>
              <div v-if="userCandidates.length === 0 && !userLookupLoading" class="table-state">没有找到可用用户</div>
              <div v-if="userLookupLoading" class="table-state">正在搜索用户…</div>
            </div>
          </div>

          <div class="field field-span-2 selection-panel">
            <div class="selection-header">
              <div>
                <span>选择机位</span>
                <small>仅显示当前空闲机位</small>
              </div>
              <div class="filter-bar compact-bar">
                <input
                  v-model.trim="openForm.machineKeyword"
                  class="input"
                  placeholder="机位编号"
                  @keyup.enter.prevent="searchMachines()"
                />
                <button
                  class="ghost-btn small-btn"
                  type="button"
                  :disabled="machineLookupLoading"
                  @click="searchMachines()"
                >
                  搜索机位
                </button>
              </div>
            </div>

            <div class="selection-grid">
              <button
                v-for="machine in availableMachines"
                :key="machine.id"
                type="button"
                class="selection-item"
                :class="selectedMachine?.id === machine.id ? 'selection-item-active' : ''"
                @click="selectMachine(machine)"
              >
                <strong>{{ machine.code }}</strong>
                <span>{{ machine.configSummary || '未配置' }}</span>
                <span>{{ machine.priceLabel }}</span>
              </button>
              <div v-if="availableMachines.length === 0 && !machineLookupLoading" class="table-state">没有找到空闲机位</div>
              <div v-if="machineLookupLoading" class="table-state">正在搜索机位…</div>
            </div>
          </div>

          <div class="field field-span-2 selected-strip">
            <div class="selected-card">
              <span class="selected-label">已选用户</span>
              <strong>{{ selectedUser ? `${selectedUser.name} · ${selectedUser.mobileMasked}` : '未选择' }}</strong>
            </div>
            <div class="selected-card">
              <span class="selected-label">已选机位</span>
              <strong>{{ selectedMachine ? `${selectedMachine.code} · ${selectedMachine.priceLabel}` : '未选择' }}</strong>
            </div>
          </div>

          <p v-if="openError" class="form-error field-span-2">{{ openError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closeOpenModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : '确认开通上机' }}
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
  type MachineItem,
  type NoticeState,
  type PagedResponse,
  type SessionItem,
  type UserLookupItem,
  formatCurrency,
  formatDateTime,
  formatMinutes,
  readErrorMessage,
  trimToUndefined
} from './shared'

const defaultCurrentFilters = () => ({
  keyword: ''
})
const defaultHistoryFilters = () => ({
  keyword: '',
  status: '',
  dateFrom: '',
  dateTo: ''
})
const currentFilters = reactive(defaultCurrentFilters())
const historyFilters = reactive(defaultHistoryFilters())
const openForm = reactive({
  userKeyword: '',
  machineKeyword: ''
})

const currentSessions = ref<SessionItem[]>([])
const historySessions = ref<SessionItem[]>([])
const userCandidates = ref<UserLookupItem[]>([])
const availableMachines = ref<MachineItem[]>([])
const selectedUser = ref<UserLookupItem | null>(null)
const selectedMachine = ref<MachineItem | null>(null)

const currentTotal = ref(0)
const currentPage = ref(1)
const currentSize = ref(10)
const historyTotal = ref(0)
const historyPage = ref(1)
const historySize = ref(10)

const loadingCurrent = ref(false)
const loadingHistory = ref(false)
const submitting = ref(false)
const userLookupLoading = ref(false)
const machineLookupLoading = ref(false)
const actionLoadingId = ref<number | null>(null)

const showOpenModal = ref(false)
const openError = ref('')
const notice = reactive<NoticeState>({ type: 'success', message: '' })

const currentTotalPages = computed(() => Math.max(1, Math.ceil(currentTotal.value / currentSize.value)))
const historyTotalPages = computed(() => Math.max(1, Math.ceil(historyTotal.value / historySize.value)))

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const clearNotice = () => {
  notice.message = ''
}

const fetchCurrentSessions = async (targetPage = currentPage.value) => {
  loadingCurrent.value = true
  clearNotice()
  try {
    const data = await requestJson<PagedResponse<SessionItem>>(
      withQuery('/sessions/current', {
        keyword: trimToUndefined(currentFilters.keyword),
        page: targetPage,
        size: currentSize.value
      })
    )
    currentSessions.value = data.items
    currentTotal.value = data.total
    currentPage.value = data.page
    currentSize.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载进行中订单失败'))
  } finally {
    loadingCurrent.value = false
  }
}

const fetchHistorySessions = async (targetPage = historyPage.value) => {
  loadingHistory.value = true
  clearNotice()
  try {
    const data = await requestJson<PagedResponse<SessionItem>>(
      withQuery('/sessions/history', {
        keyword: trimToUndefined(historyFilters.keyword),
        status: historyFilters.status,
        dateFrom: historyFilters.dateFrom,
        dateTo: historyFilters.dateTo,
        page: targetPage,
        size: historySize.value
      })
    )
    historySessions.value = data.items
    historyTotal.value = data.total
    historyPage.value = data.page
    historySize.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载历史订单失败'))
  } finally {
    loadingHistory.value = false
  }
}

const refreshAll = () => {
  void Promise.all([fetchCurrentSessions(currentPage.value), fetchHistorySessions(historyPage.value)])
}

const applyCurrentFilters = () => {
  void fetchCurrentSessions(1)
}

const resetCurrentFilters = () => {
  Object.assign(currentFilters, defaultCurrentFilters())
  void fetchCurrentSessions(1)
}

const changeCurrentPage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > currentTotalPages.value || targetPage === currentPage.value) {
    return
  }
  void fetchCurrentSessions(targetPage)
}

const applyHistoryFilters = () => {
  void fetchHistorySessions(1)
}

const resetHistoryFilters = () => {
  Object.assign(historyFilters, defaultHistoryFilters())
  void fetchHistorySessions(1)
}

const changeHistoryPage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > historyTotalPages.value || targetPage === historyPage.value) {
    return
  }
  void fetchHistorySessions(targetPage)
}

const searchUsers = async () => {
  userLookupLoading.value = true
  openError.value = ''
  try {
    const data = await requestJson<PagedResponse<UserLookupItem>>(
      withQuery('/users', {
        keyword: trimToUndefined(openForm.userKeyword),
        status: 1,
        page: 1,
        size: 8
      })
    )
    userCandidates.value = data.items
  } catch (error) {
    openError.value = readErrorMessage(error, '搜索用户失败')
  } finally {
    userLookupLoading.value = false
  }
}

const searchMachines = async () => {
  machineLookupLoading.value = true
  openError.value = ''
  try {
    const data = await requestJson<PagedResponse<MachineItem>>(
      withQuery('/machines', {
        keyword: trimToUndefined(openForm.machineKeyword),
        status: 0,
        page: 1,
        size: 8
      })
    )
    availableMachines.value = data.items
  } catch (error) {
    openError.value = readErrorMessage(error, '搜索机位失败')
  } finally {
    machineLookupLoading.value = false
  }
}

const selectUser = (user: UserLookupItem) => {
  selectedUser.value = user
}

const selectMachine = (machine: MachineItem) => {
  selectedMachine.value = machine
}

const openOpenModal = async () => {
  showOpenModal.value = true
  openError.value = ''
  openForm.userKeyword = ''
  openForm.machineKeyword = ''
  selectedUser.value = null
  selectedMachine.value = null
  await Promise.all([searchUsers(), searchMachines()])
}

const closeOpenModal = () => {
  if (submitting.value) {
    return
  }
  showOpenModal.value = false
  openError.value = ''
}

const submitOpenSession = async () => {
  if (!selectedUser.value) {
    openError.value = '请选择用户'
    return
  }
  if (!selectedMachine.value) {
    openError.value = '请选择机位'
    return
  }

  submitting.value = true
  clearNotice()
  try {
    await requestJson<boolean>('/sessions/open', {
      method: 'POST',
      body: JSON.stringify({
        userId: selectedUser.value.id,
        machineId: selectedMachine.value.id
      })
    })
    setNotice('success', `已为 ${selectedUser.value.name} 开通 ${selectedMachine.value.code} 上机`)
    showOpenModal.value = false
    openError.value = ''
    await Promise.all([fetchCurrentSessions(1), fetchHistorySessions(1)])
  } catch (error) {
    openError.value = readErrorMessage(error, '开通上机失败')
  } finally {
    submitting.value = false
  }
}

const forceEnd = async (row: SessionItem) => {
  const userName = row.userName || `用户 #${row.userId}`
  const machineCode = row.machineCode || `机位 #${row.machineId}`
  if (!window.confirm(`确认强制结束 ${userName} 在 ${machineCode} 的上机订单吗？`)) {
    return
  }

  actionLoadingId.value = row.id
  clearNotice()
  try {
    await requestJson<boolean>(`/sessions/${row.id}/force-end`, {
      method: 'PUT'
    })
    setNotice('success', `已强制结束 ${userName} 的上机订单`)
    await Promise.all([fetchCurrentSessions(currentPage.value), fetchHistorySessions(1)])
  } catch (error) {
    setNotice('error', readErrorMessage(error, '强制下机失败'))
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(async () => {
  await Promise.all([fetchCurrentSessions(), fetchHistorySessions()])
})
</script>

<style scoped>
.modal-card-xl {
  width: min(980px, calc(100vw - 32px));
}

.page {
  min-height: 100%;
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

.input-date {
  max-width: 150px;
}

.compact-bar {
  gap: 8px;
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
  color: #867b6d;
  font-size: 12px;
}

.selection-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.selection-item {
  padding: 12px 14px;
  border: 1px solid rgba(39, 35, 29, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  text-align: left;
  display: grid;
  gap: 4px;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.selection-item strong {
  color: #282018;
  font-size: 14px;
  font-weight: 700;
}

.selection-item span {
  color: #7e7466;
  font-size: 12px;
}

.selection-item:hover {
  transform: translateY(-1px);
  border-color: rgba(255, 153, 43, 0.45);
  box-shadow: 0 12px 24px rgba(255, 153, 43, 0.08);
}

.selection-item-active {
  border-color: rgba(255, 153, 43, 0.65);
  background: rgba(255, 247, 238, 0.96);
  box-shadow: 0 14px 28px rgba(255, 153, 43, 0.12);
}

.selected-strip {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.selected-card {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(39, 35, 29, 0.04);
  display: grid;
  gap: 6px;
}

.selected-label {
  color: #8a7e6f;
  font-size: 12px;
}

.selected-card strong {
  color: #2d241b;
  font-size: 14px;
}

@media (max-width: 900px) {
  .selected-strip {
    grid-template-columns: 1fr;
  }

  .selection-header {
    flex-direction: column;
  }
}
</style>
