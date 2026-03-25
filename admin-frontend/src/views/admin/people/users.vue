<template>
  <section class="page">
    <AppTopbar
      title="用户管理"
      subtitle="注册、余额与状态"
      eyebrow="用户"
      action-label="刷新列表"
      :action-disabled="loading"
      :action-handler="fetchUsers"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card page-card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>手机号 / 身份证 / 状态</h3>
        </div>
        <div class="filter-bar">
          <input
            v-model.trim="filters.keyword"
            class="input"
            placeholder="姓名/手机号/身份证"
            @keyup.enter="applyFilters"
          />
          <select v-model="filters.status" class="select">
            <option value="">全部状态</option>
            <option value="1">可用</option>
            <option value="0">冻结</option>
          </select>
          <button class="ghost-btn" :disabled="loading" @click="applyFilters">查询</button>
          <button class="ghost-btn" :disabled="loading" @click="resetFilters">重置</button>
          <button class="solid-btn" :disabled="submitting" @click="openCreateModal">新增用户</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>手机号</th>
              <th>身份证</th>
              <th>余额</th>
              <th>状态</th>
              <th>最近上机</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="userList.length > 0">
            <tr v-for="row in userList" :key="row.id">
              <td>{{ row.name }}</td>
              <td>{{ row.mobileMasked }}</td>
              <td>{{ row.idCardMasked }}</td>
              <td>{{ formatCurrency(row.balance) }}</td>
              <td>
                <StatusPill :text="row.statusLabel" :tone="row.status === 1 ? 'idle' : 'disabled'" />
              </td>
              <td>{{ formatDateTime(row.lastSessionTime) }}</td>
              <td>{{ formatDateTime(row.registerTime) }}</td>
              <td>
                <div class="table-actions">
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="openEditModal(row.id)">
                    编辑
                  </button>
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="openRechargeModal(row)">
                    充值
                  </button>
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="toggleStatus(row)">
                    {{ row.status === 1 ? '冻结' : '解冻' }}
                  </button>
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="openRecordsModal(row.id)">
                    记录
                  </button>
                  <button class="ghost-btn small-btn danger-btn" :disabled="actionLoadingId === row.id" @click="removeUser(row)">
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loading" class="table-state">正在加载用户列表…</div>
        <div v-else-if="userList.length === 0" class="table-state">当前筛选条件下没有用户数据</div>
      </div>

      <div class="pagination-bar">
        <p class="pagination-text">共 {{ total }} 条，当前第 {{ page }} / {{ totalPages }} 页</p>
        <div class="table-actions">
          <button class="ghost-btn small-btn" :disabled="loading || page <= 1" @click="changePage(page - 1)">上一页</button>
          <button class="ghost-btn small-btn" :disabled="loading || page >= totalPages" @click="changePage(page + 1)">下一页</button>
        </div>
      </div>
    </div>

    <div v-if="showUserModal" class="modal-overlay" @click.self="closeUserModal">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <p class="eyebrow">{{ userFormMode === 'create' ? '新增' : '编辑' }}</p>
            <h3>{{ userFormMode === 'create' ? '新增用户' : '编辑用户' }}</h3>
          </div>
          <button class="ghost-btn small-btn" @click="closeUserModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitUserForm">
          <label class="field">
            <span>姓名</span>
            <input v-model.trim="userForm.name" class="input" :disabled="submitting" placeholder="请输入姓名" />
          </label>

          <label class="field">
            <span>手机号</span>
            <input v-model.trim="userForm.mobile" class="input" :disabled="submitting" placeholder="请输入手机号" />
          </label>

          <label class="field">
            <span>身份证</span>
            <input v-model.trim="userForm.idCard" class="input" :disabled="submitting" placeholder="请输入身份证" />
          </label>

          <label class="field">
            <span>状态</span>
            <select v-model="userForm.status" class="select" :disabled="submitting">
              <option value="1">可用</option>
              <option value="0">冻结</option>
            </select>
          </label>

          <label v-if="userFormMode === 'create'" class="field field-span-2">
            <span>初始余额</span>
            <input
              v-model.trim="userForm.balance"
              class="input"
              type="number"
              min="0"
              step="0.01"
              :disabled="submitting"
              placeholder="默认 0.00"
            />
          </label>

          <div v-else class="record-grid field-span-2">
            <div class="record-card">
              <p class="eyebrow">当前余额</p>
              <strong>{{ formatCurrency(userForm.currentBalance || 0) }}</strong>
            </div>
            <div class="record-card">
              <p class="eyebrow">最近上机</p>
              <strong>{{ formatDateTime(userForm.lastSessionTime) }}</strong>
            </div>
          </div>

          <p v-if="userFormError" class="form-error field-span-2">{{ userFormError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closeUserModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : userFormMode === 'create' ? '创建用户' : '保存修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showRechargeModal" class="modal-overlay" @click.self="closeRechargeModal">
      <div class="modal-card modal-card-sm">
        <div class="modal-header">
          <div>
            <p class="eyebrow">充值</p>
            <h3>为 {{ rechargeForm.name }} 充值</h3>
          </div>
          <button class="ghost-btn small-btn" @click="closeRechargeModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitRecharge">
          <label class="field">
            <span>充值金额</span>
            <input
              v-model.trim="rechargeForm.amount"
              class="input"
              type="number"
              min="0.01"
              step="0.01"
              :disabled="submitting"
              placeholder="请输入充值金额"
            />
          </label>

          <label class="field">
            <span>充值渠道</span>
            <input
              v-model.trim="rechargeForm.channel"
              class="input"
              :disabled="submitting"
              placeholder="例如：现金 / 微信"
            />
          </label>

          <label class="field field-span-2">
            <span>备注</span>
            <input
              v-model.trim="rechargeForm.remark"
              class="input"
              :disabled="submitting"
              placeholder="可填写前台充值、活动赠送等"
            />
          </label>

          <p v-if="rechargeError" class="form-error field-span-2">{{ rechargeError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closeRechargeModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : '确认充值' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showRecordsModal" class="modal-overlay" @click.self="closeRecordsModal">
      <div class="modal-card modal-card-wide">
        <div class="modal-header">
          <div>
            <p class="eyebrow">记录</p>
            <h3>用户记录</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="recordsLoading" @click="closeRecordsModal">关闭</button>
        </div>

        <div v-if="recordDetail" class="record-grid">
          <div class="record-card">
            <p class="eyebrow">用户姓名</p>
            <strong>{{ recordDetail.name }}</strong>
          </div>
          <div class="record-card">
            <p class="eyebrow">手机号</p>
            <strong>{{ recordDetail.mobileMasked }}</strong>
          </div>
          <div class="record-card">
            <p class="eyebrow">当前余额</p>
            <strong>{{ formatCurrency(recordDetail.balance) }}</strong>
          </div>
          <div class="record-card">
            <p class="eyebrow">状态</p>
            <strong>{{ recordDetail.statusLabel }}</strong>
          </div>
        </div>

        <p v-if="recordsError" class="form-error">{{ recordsError }}</p>

        <div class="records-section">
          <div class="section-heading">
            <div>
              <p class="eyebrow">充值记录</p>
              <h4>按时间倒序</h4>
            </div>
          </div>
          <div class="table-wrap mini-table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>时间</th>
                  <th>金额</th>
                  <th>渠道</th>
                  <th>经办人ID</th>
                  <th>备注</th>
                </tr>
              </thead>
              <tbody v-if="rechargeRecords.length > 0">
                <tr v-for="record in rechargeRecords" :key="record.id">
                  <td>{{ formatDateTime(record.createdAt) }}</td>
                  <td>{{ formatCurrency(record.amount) }}</td>
                  <td>{{ record.channel }}</td>
                  <td>{{ record.operatorAdminId }}</td>
                  <td>{{ record.remark || '—' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="recordsLoading" class="table-state">正在加载记录…</div>
            <div v-else-if="rechargeRecords.length === 0" class="table-state">暂无充值记录</div>
          </div>
        </div>

        <div class="records-section">
          <div class="section-heading">
            <div>
              <p class="eyebrow">上机记录</p>
              <h4>按开始时间倒序</h4>
            </div>
          </div>
          <div class="table-wrap mini-table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>机位ID</th>
                  <th>开始时间</th>
                  <th>结束时间</th>
                  <th>时长</th>
                  <th>单价</th>
                  <th>金额</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody v-if="sessionRecords.length > 0">
                <tr v-for="record in sessionRecords" :key="record.id">
                  <td>{{ record.machineId }}</td>
                  <td>{{ formatDateTime(record.startTime) }}</td>
                  <td>{{ formatDateTime(record.endTime) }}</td>
                  <td>{{ record.durationMinutes }} 分钟</td>
                  <td>{{ formatCurrency(record.priceSnapshot) }}</td>
                  <td>{{ formatCurrency(record.amount) }}</td>
                  <td>
                    <StatusPill :text="record.statusLabel" :tone="record.status === 2 ? 'forced' : 'idle'" />
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="recordsLoading" class="table-state">正在加载记录…</div>
            <div v-else-if="sessionRecords.length === 0" class="table-state">暂无上机记录</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppTopbar from '../../../components/AppTopbar.vue'
import StatusPill from '../../../components/StatusPill.vue'
import { ApiError, requestJson, withQuery } from '../../../access/http'

type UserItem = {
  id: number
  name: string
  mobile: string
  mobileMasked: string
  idCard: string
  idCardMasked: string
  balance: number
  status: 0 | 1
  statusLabel: string
  registerTime: string | null
  lastSessionTime: string | null
  createdAt: string | null
  updatedAt: string | null
}

type UserListResponse = {
  total: number
  page: number
  size: number
  items: UserItem[]
}

type RechargeRecord = {
  id: number
  userId: number
  amount: number
  channel: string
  remark: string | null
  operatorAdminId: number
  createdAt: string
}

type SessionRecord = {
  id: number
  userId: number
  machineId: number
  durationMinutes: number
  priceSnapshot: number
  amount: number
  status: 0 | 1 | 2
  statusLabel: string
  startTime: string
  endTime: string | null
}

type NoticeState = {
  type: 'success' | 'error'
  message: string
}

const defaultUserForm = () => ({
  id: 0,
  name: '',
  mobile: '',
  idCard: '',
  status: '1',
  balance: '0.00',
  currentBalance: 0,
  lastSessionTime: null as string | null
})

const filters = reactive({
  keyword: '',
  status: ''
})

const userList = ref<UserItem[]>([])
const loading = ref(false)
const submitting = ref(false)
const actionLoadingId = ref<number | null>(null)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const notice = reactive<NoticeState>({ type: 'success', message: '' })

const showUserModal = ref(false)
const userFormMode = ref<'create' | 'edit'>('create')
const userForm = reactive(defaultUserForm())
const userFormError = ref('')

const showRechargeModal = ref(false)
const rechargeForm = reactive({
  userId: 0,
  name: '',
  amount: '',
  channel: '现金',
  remark: ''
})
const rechargeError = ref('')

const showRecordsModal = ref(false)
const recordsLoading = ref(false)
const recordsError = ref('')
const recordDetail = ref<UserItem | null>(null)
const rechargeRecords = ref<RechargeRecord[]>([])
const sessionRecords = ref<SessionRecord[]>([])

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const clearNotice = () => {
  notice.message = ''
}

const readErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof ApiError) {
    return error.message || fallback
  }
  if (error instanceof Error) {
    return error.message || fallback
  }
  return fallback
}

const formatDateTime = (value: string | null) => {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}

const fetchUsers = async (targetPage = page.value) => {
  loading.value = true
  clearNotice()
  try {
    const data = await requestJson<UserListResponse>(
      withQuery('/users', {
        keyword: filters.keyword,
        status: filters.status,
        page: targetPage,
        size: size.value
      })
    )
    userList.value = data.items
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载用户列表失败'))
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  void fetchUsers(1)
}

const resetFilters = () => {
  filters.keyword = ''
  filters.status = ''
  void fetchUsers(1)
}

const changePage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > totalPages.value || targetPage === page.value) {
    return
  }
  void fetchUsers(targetPage)
}

const resetUserForm = () => {
  Object.assign(userForm, defaultUserForm())
  userFormError.value = ''
}

const openCreateModal = () => {
  userFormMode.value = 'create'
  resetUserForm()
  showUserModal.value = true
}

const openEditModal = async (id: number) => {
  actionLoadingId.value = id
  userFormMode.value = 'edit'
  userFormError.value = ''
  try {
    const detail = await requestJson<UserItem>(`/users/${id}`)
    Object.assign(userForm, {
      id: detail.id,
      name: detail.name,
      mobile: detail.mobile,
      idCard: detail.idCard,
      status: String(detail.status),
      balance: '',
      currentBalance: detail.balance,
      lastSessionTime: detail.lastSessionTime
    })
    showUserModal.value = true
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载用户详情失败'))
  } finally {
    actionLoadingId.value = null
  }
}

const closeUserModal = () => {
  if (submitting.value) {
    return
  }
  showUserModal.value = false
  resetUserForm()
}

const validateUserForm = () => {
  if (!userForm.name) {
    return '请输入姓名'
  }
  if (!userForm.mobile) {
    return '请输入手机号'
  }
  if (!userForm.idCard) {
    return '请输入身份证'
  }
  if (userFormMode.value === 'create' && userForm.balance && Number(userForm.balance) < 0) {
    return '初始余额不能小于 0'
  }
  return ''
}

const submitUserForm = async () => {
  userFormError.value = validateUserForm()
  if (userFormError.value) {
    return
  }

  submitting.value = true
  clearNotice()
  try {
    if (userFormMode.value === 'create') {
      await requestJson<boolean>('/users', {
        method: 'POST',
        body: JSON.stringify({
          name: userForm.name,
          mobile: userForm.mobile,
          idCard: userForm.idCard,
          balance: userForm.balance ? Number(userForm.balance) : 0,
          status: Number(userForm.status)
        })
      })
      setNotice('success', '用户已创建')
    } else {
      await requestJson<boolean>(`/users/${userForm.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          name: userForm.name,
          mobile: userForm.mobile,
          idCard: userForm.idCard,
          status: Number(userForm.status)
        })
      })
      setNotice('success', '用户信息已更新')
    }

    showUserModal.value = false
    resetUserForm()
    await fetchUsers(userFormMode.value === 'create' ? 1 : page.value)
  } catch (error) {
    userFormError.value = readErrorMessage(error, '提交用户信息失败')
  } finally {
    submitting.value = false
  }
}

const openRechargeModal = (row: UserItem) => {
  rechargeForm.userId = row.id
  rechargeForm.name = row.name
  rechargeForm.amount = ''
  rechargeForm.channel = '现金'
  rechargeForm.remark = ''
  rechargeError.value = ''
  showRechargeModal.value = true
}

const closeRechargeModal = () => {
  if (submitting.value) {
    return
  }
  showRechargeModal.value = false
  rechargeForm.userId = 0
  rechargeForm.name = ''
  rechargeForm.amount = ''
  rechargeForm.channel = '现金'
  rechargeForm.remark = ''
  rechargeError.value = ''
}

const submitRecharge = async () => {
  if (!rechargeForm.amount || Number(rechargeForm.amount) <= 0) {
    rechargeError.value = '请输入大于 0 的充值金额'
    return
  }
  if (!rechargeForm.channel) {
    rechargeError.value = '请输入充值渠道'
    return
  }

  submitting.value = true
  clearNotice()
  try {
    await requestJson<boolean>(`/users/${rechargeForm.userId}/recharge`, {
      method: 'POST',
      body: JSON.stringify({
        amount: Number(rechargeForm.amount),
        channel: rechargeForm.channel,
        remark: rechargeForm.remark || null
      })
    })
    setNotice('success', `已为 ${rechargeForm.name} 完成充值`)
    closeRechargeModal()
    await fetchUsers(page.value)
  } catch (error) {
    rechargeError.value = readErrorMessage(error, '充值失败')
  } finally {
    submitting.value = false
  }
}

const openRecordsModal = async (id: number) => {
  showRecordsModal.value = true
  recordsLoading.value = true
  recordsError.value = ''
  recordDetail.value = null
  rechargeRecords.value = []
  sessionRecords.value = []

  try {
    const [detail, recharges, sessions] = await Promise.all([
      requestJson<UserItem>(`/users/${id}`),
      requestJson<RechargeRecord[]>(`/users/${id}/recharges`),
      requestJson<SessionRecord[]>(`/users/${id}/sessions`)
    ])
    recordDetail.value = detail
    rechargeRecords.value = recharges
    sessionRecords.value = sessions
  } catch (error) {
    recordsError.value = readErrorMessage(error, '加载用户记录失败')
  } finally {
    recordsLoading.value = false
  }
}

const closeRecordsModal = () => {
  if (recordsLoading.value) {
    return
  }
  showRecordsModal.value = false
  recordsError.value = ''
  recordDetail.value = null
  rechargeRecords.value = []
  sessionRecords.value = []
}

const toggleStatus = async (row: UserItem) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionLabel = nextStatus === 1 ? '解冻' : '冻结'
  if (!window.confirm(`确认${actionLabel}用户 ${row.name} 吗？`)) {
    return
  }

  actionLoadingId.value = row.id
  clearNotice()
  try {
    await requestJson<boolean>(`/users/${row.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: nextStatus })
    })
    setNotice('success', `已${actionLabel}用户 ${row.name}`)
    await fetchUsers(page.value)
  } catch (error) {
    setNotice('error', readErrorMessage(error, `${actionLabel}用户失败`))
  } finally {
    actionLoadingId.value = null
  }
}

const removeUser = async (row: UserItem) => {
  if (!window.confirm(`确认删除用户 ${row.name} 吗？该操作不可撤销。`)) {
    return
  }

  actionLoadingId.value = row.id
  clearNotice()
  try {
    await requestJson<boolean>(`/users/${row.id}`, {
      method: 'DELETE'
    })
    setNotice('success', `已删除用户 ${row.name}`)
    await fetchUsers(page.value > 1 && userList.value.length === 1 ? page.value - 1 : page.value)
  } catch (error) {
    setNotice('error', readErrorMessage(error, '删除用户失败'))
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(() => {
  void fetchUsers()
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
</style>
