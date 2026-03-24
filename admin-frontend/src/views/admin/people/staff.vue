<template>
  <section class="page">
    <AppTopbar title="员工管理" subtitle="仅超级管理员可见" eyebrow="人员">
      <template #actions>
        <button class="ghost-btn" :disabled="loading" @click="fetchStaff()">刷新列表</button>
      </template>
    </AppTopbar>

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>账号与角色</h3>
        </div>
        <div class="filter-bar">
          <input
            v-model.trim="filters.keyword"
            class="input"
            placeholder="账号/姓名"
            @keyup.enter="applyFilters"
          />
          <select v-model="filters.role" class="select">
            <option value="">全部角色</option>
            <option v-for="role in roleOptions" :key="role.code" :value="role.code">
              {{ role.label }}
            </option>
          </select>
          <select v-model="filters.status" class="select">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
          <button class="ghost-btn" :disabled="loading" @click="applyFilters">查询</button>
          <button class="ghost-btn" :disabled="loading" @click="resetFilters">重置</button>
          <button class="solid-btn" :disabled="submitting" @click="openCreateModal">新增员工</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>账号</th>
              <th>姓名</th>
              <th>角色</th>
              <th>状态</th>
              <th>最近登录</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="staffList.length > 0">
            <tr v-for="row in staffList" :key="row.id">
              <td>{{ row.username }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.roleLabel }}</td>
              <td>
                <StatusPill :text="row.statusLabel" :tone="row.status === 1 ? 'idle' : 'disabled'" />
              </td>
              <td>{{ formatDateTime(row.lastLoginTime) }}</td>
              <td>{{ formatDateTime(row.createdAt) }}</td>
              <td>
                <div class="table-actions">
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="openEditModal(row.id)">
                    编辑
                  </button>
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="openResetPasswordModal(row)">
                    重置密码
                  </button>
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="toggleStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button class="ghost-btn small-btn danger-btn" :disabled="actionLoadingId === row.id" @click="removeStaff(row)">
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loading" class="table-state">正在加载员工列表…</div>
        <div v-else-if="staffList.length === 0" class="table-state">当前筛选条件下没有员工数据</div>
      </div>

      <div class="pagination-bar">
        <p class="pagination-text">共 {{ total }} 条，当前第 {{ page }} / {{ totalPages }} 页</p>
        <div class="table-actions">
          <button class="ghost-btn small-btn" :disabled="loading || page <= 1" @click="changePage(page - 1)">上一页</button>
          <button class="ghost-btn small-btn" :disabled="loading || page >= totalPages" @click="changePage(page + 1)">下一页</button>
        </div>
      </div>
    </div>

    <div v-if="showFormModal" class="modal-overlay" @click.self="closeFormModal">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <p class="eyebrow">{{ formMode === 'create' ? '新增' : '编辑' }}</p>
            <h3>{{ formMode === 'create' ? '新增员工' : '编辑员工' }}</h3>
          </div>
          <button class="ghost-btn small-btn" @click="closeFormModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitStaffForm">
          <label class="field">
            <span>账号</span>
            <input
              v-model.trim="staffForm.username"
              class="input"
              :disabled="formMode === 'edit' || submitting"
              placeholder="请输入登录账号"
            />
          </label>

          <label class="field">
            <span>姓名</span>
            <input v-model.trim="staffForm.name" class="input" :disabled="submitting" placeholder="请输入姓名" />
          </label>

          <label class="field">
            <span>角色</span>
            <select v-model="staffForm.role" class="select" :disabled="submitting">
              <option value="">请选择角色</option>
              <option v-for="role in roleOptions" :key="role.code" :value="role.code">
                {{ role.label }}
              </option>
            </select>
          </label>

          <label class="field">
            <span>状态</span>
            <select v-model="staffForm.status" class="select" :disabled="submitting">
              <option value="1">启用</option>
              <option value="0">禁用</option>
            </select>
          </label>

          <label class="field field-span-2">
            <span>{{ formMode === 'create' ? '初始密码' : '登录密码（留空则不修改）' }}</span>
            <input
              v-model="staffForm.password"
              class="input"
              type="password"
              :disabled="submitting"
              :placeholder="formMode === 'create' ? '至少 6 位' : '如需一并修改密码，可直接输入新密码'"
            />
          </label>

          <p v-if="formError" class="form-error field-span-2">{{ formError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closeFormModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : formMode === 'create' ? '创建员工' : '保存修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showPasswordModal" class="modal-overlay" @click.self="closePasswordModal">
      <div class="modal-card modal-card-sm">
        <div class="modal-header">
          <div>
            <p class="eyebrow">安全</p>
            <h3>重置密码</h3>
          </div>
          <button class="ghost-btn small-btn" @click="closePasswordModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitPasswordReset">
          <label class="field field-span-2">
            <span>新密码</span>
            <input
              v-model="passwordForm.password"
              class="input"
              type="password"
              :disabled="submitting"
              placeholder="请输入新密码"
            />
          </label>

          <label class="field field-span-2">
            <span>确认新密码</span>
            <input
              v-model="passwordForm.confirmPassword"
              class="input"
              type="password"
              :disabled="submitting"
              placeholder="请再次输入新密码"
            />
          </label>

          <p v-if="passwordError" class="form-error field-span-2">{{ passwordError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closePasswordModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : '确认重置' }}
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
import { ApiError, requestJson, withQuery } from '../../../access/http'

type RoleOption = {
  code: 'SUPER_ADMIN' | 'ADMIN'
  label: string
  description: string
}

type StaffItem = {
  id: number
  username: string
  name: string
  role: RoleOption['code']
  roleLabel: string
  status: 0 | 1
  statusLabel: string
  lastLoginTime: string | null
  createdAt: string | null
  updatedAt: string | null
}

type StaffListResponse = {
  total: number
  page: number
  size: number
  items: StaffItem[]
}

type NoticeState = {
  type: 'success' | 'error'
  message: string
}

const defaultStaffForm = () => ({
  id: 0,
  username: '',
  name: '',
  role: '',
  status: '1',
  password: ''
})

const filters = reactive({
  keyword: '',
  role: '',
  status: ''
})

const roleOptions = ref<RoleOption[]>([])
const staffList = ref<StaffItem[]>([])
const loading = ref(false)
const submitting = ref(false)
const actionLoadingId = ref<number | null>(null)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const notice = reactive<NoticeState>({ type: 'success', message: '' })

const showFormModal = ref(false)
const showPasswordModal = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const staffForm = reactive(defaultStaffForm())
const passwordForm = reactive({
  staffId: 0,
  password: '',
  confirmPassword: '',
  username: ''
})
const formError = ref('')
const passwordError = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const clearNotice = () => {
  notice.message = ''
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

const readErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof ApiError) {
    return error.message || fallback
  }
  if (error instanceof Error) {
    return error.message || fallback
  }
  return fallback
}

const fetchRoleOptions = async () => {
  roleOptions.value = await requestJson<RoleOption[]>('/admins/role-options')
}

const fetchStaff = async (targetPage = page.value) => {
  loading.value = true
  clearNotice()
  try {
    const data = await requestJson<StaffListResponse>(
      withQuery('/admins', {
        keyword: filters.keyword,
        role: filters.role,
        status: filters.status,
        page: targetPage,
        size: size.value
      })
    )
    staffList.value = data.items
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载员工列表失败'))
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  void fetchStaff(1)
}

const resetFilters = () => {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  void fetchStaff(1)
}

const changePage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > totalPages.value || targetPage === page.value) {
    return
  }
  void fetchStaff(targetPage)
}

const resetStaffForm = () => {
  Object.assign(staffForm, defaultStaffForm())
  formError.value = ''
}

const openCreateModal = () => {
  formMode.value = 'create'
  resetStaffForm()
  showFormModal.value = true
}

const openEditModal = async (id: number) => {
  actionLoadingId.value = id
  formMode.value = 'edit'
  formError.value = ''
  try {
    const detail = await requestJson<StaffItem>(`/admins/${id}`)
    Object.assign(staffForm, {
      id: detail.id,
      username: detail.username,
      name: detail.name,
      role: detail.role,
      status: String(detail.status),
      password: ''
    })
    showFormModal.value = true
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载员工详情失败'))
  } finally {
    actionLoadingId.value = null
  }
}

const closeFormModal = () => {
  if (submitting.value) {
    return
  }
  showFormModal.value = false
  resetStaffForm()
}

const validateStaffForm = () => {
  if (formMode.value === 'create' && !staffForm.username) {
    return '请输入账号'
  }
  if (!staffForm.name) {
    return '请输入姓名'
  }
  if (!staffForm.role) {
    return '请选择角色'
  }
  if (formMode.value === 'create' && staffForm.password.length < 6) {
    return '初始密码至少 6 位'
  }
  if (formMode.value === 'edit' && staffForm.password && staffForm.password.length < 6) {
    return '密码至少 6 位'
  }
  return ''
}

const submitStaffForm = async () => {
  formError.value = validateStaffForm()
  if (formError.value) {
    return
  }

  submitting.value = true
  clearNotice()
  try {
    if (formMode.value === 'create') {
      await requestJson<boolean>('/admins', {
        method: 'POST',
        body: JSON.stringify({
          username: staffForm.username,
          password: staffForm.password,
          name: staffForm.name,
          role: staffForm.role,
          status: Number(staffForm.status)
        })
      })
      setNotice('success', '员工已创建')
    } else {
      const payload: Record<string, string | number> = {
        name: staffForm.name,
        role: staffForm.role,
        status: Number(staffForm.status)
      }
      if (staffForm.password) {
        payload.password = staffForm.password
      }
      await requestJson<boolean>(`/admins/${staffForm.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
      setNotice('success', '员工信息已更新')
    }
    showFormModal.value = false
    resetStaffForm()
    await fetchStaff(formMode.value === 'create' ? 1 : page.value)
  } catch (error) {
    formError.value = readErrorMessage(error, '提交员工信息失败')
  } finally {
    submitting.value = false
  }
}

const openResetPasswordModal = (row: StaffItem) => {
  passwordForm.staffId = row.id
  passwordForm.username = row.username
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  passwordError.value = ''
  showPasswordModal.value = true
}

const closePasswordModal = () => {
  if (submitting.value) {
    return
  }
  showPasswordModal.value = false
  passwordForm.staffId = 0
  passwordForm.username = ''
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  passwordError.value = ''
}

const submitPasswordReset = async () => {
  if (passwordForm.password.length < 6) {
    passwordError.value = '新密码至少 6 位'
    return
  }
  if (passwordForm.password !== passwordForm.confirmPassword) {
    passwordError.value = '两次输入的密码不一致'
    return
  }

  submitting.value = true
  clearNotice()
  try {
    await requestJson<boolean>(`/admins/${passwordForm.staffId}/password-reset`, {
      method: 'PUT',
      body: JSON.stringify({
        password: passwordForm.password
      })
    })
    setNotice('success', `已重置 ${passwordForm.username} 的密码`)
    closePasswordModal()
  } catch (error) {
    passwordError.value = readErrorMessage(error, '重置密码失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: StaffItem) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionLabel = nextStatus === 1 ? '启用' : '禁用'
  if (!window.confirm(`确认${actionLabel}员工 ${row.username} 吗？`)) {
    return
  }

  actionLoadingId.value = row.id
  clearNotice()
  try {
    await requestJson<boolean>(`/admins/${row.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: nextStatus })
    })
    setNotice('success', `已${actionLabel}员工 ${row.username}`)
    await fetchStaff(page.value)
  } catch (error) {
    setNotice('error', readErrorMessage(error, `${actionLabel}员工失败`))
  } finally {
    actionLoadingId.value = null
  }
}

const removeStaff = async (row: StaffItem) => {
  if (!window.confirm(`确认删除员工 ${row.username} 吗？该操作不可撤销。`)) {
    return
  }

  actionLoadingId.value = row.id
  clearNotice()
  try {
    await requestJson<boolean>(`/admins/${row.id}`, {
      method: 'DELETE'
    })
    setNotice('success', `已删除员工 ${row.username}`)
    await fetchStaff(page.value > 1 && staffList.value.length === 1 ? page.value - 1 : page.value)
  } catch (error) {
    setNotice('error', readErrorMessage(error, '删除员工失败'))
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(async () => {
  try {
    await fetchRoleOptions()
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载角色选项失败'))
  }
  await fetchStaff()
})
</script>
