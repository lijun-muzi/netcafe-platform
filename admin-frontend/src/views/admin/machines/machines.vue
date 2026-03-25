<template>
  <section class="page">
    <AppTopbar
      title="机位管理"
      subtitle="配置、单价与维护状态"
      eyebrow="设备"
      action-label="刷新列表"
      :action-disabled="loading"
      :action-handler="fetchMachines"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card page-card">
      <div class="card-header">
        <div>
          <p class="eyebrow">筛选</p>
          <h3>编号 / 状态 / 单价区间</h3>
        </div>
        <div class="filter-bar">
          <input
            v-model.trim="filters.keyword"
            class="input"
            placeholder="机位编号"
            @keyup.enter="applyFilters"
          />
          <select v-model="filters.status" class="select">
            <option value="">全部状态</option>
            <option value="0">空闲</option>
            <option value="1">使用中</option>
            <option value="2">停用</option>
          </select>
          <input
            v-model.trim="filters.minPrice"
            class="input input-sm"
            inputmode="decimal"
            placeholder="最低单价"
            @keyup.enter="applyFilters"
          />
          <input
            v-model.trim="filters.maxPrice"
            class="input input-sm"
            inputmode="decimal"
            placeholder="最高单价"
            @keyup.enter="applyFilters"
          />
          <button class="ghost-btn" :disabled="loading" @click="applyFilters">查询</button>
          <button class="ghost-btn" :disabled="loading" @click="resetFilters">重置</button>
          <button class="ghost-btn" :disabled="submitting" @click="openBatchModal">模板批量建机位</button>
          <button class="solid-btn" :disabled="submitting" @click="openCreateModal">新增机位</button>
        </div>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>编号</th>
              <th>状态</th>
              <th>单价</th>
              <th>配置摘要</th>
              <th>当前使用</th>
              <th>维护时间</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="machines.length > 0">
            <tr v-for="row in machines" :key="row.id">
              <td>{{ row.code }}</td>
              <td>
                <StatusPill :text="row.statusLabel" :tone="row.statusTone" />
              </td>
              <td>{{ row.priceLabel }}</td>
              <td>
                <div class="machine-summary">
                  <strong>{{ row.configSummary || '未配置' }}</strong>
                  <span v-if="resolveZone(row)">{{ resolveZone(row) }}</span>
                </div>
              </td>
              <td>
                <div v-if="row.currentUserName || row.currentDurationMinutes !== null" class="machine-summary">
                  <strong>{{ row.currentUserName || '进行中订单' }}</strong>
                  <span>
                    {{ formatMinutes(row.currentDurationMinutes) }} · {{ formatCurrency(row.currentFee) }}
                  </span>
                </div>
                <span v-else>—</span>
              </td>
              <td>{{ formatDateTime(row.lastMaintainedAt) }}</td>
              <td>{{ formatDate(row.createdAt) }}</td>
              <td>
                <div class="table-actions">
                  <button class="ghost-btn small-btn" :disabled="actionLoadingId === row.id" @click="openEditModal(row.id)">
                    编辑
                  </button>
                  <button
                    v-if="isSuperAdmin"
                    class="ghost-btn small-btn"
                    :disabled="actionLoadingId === row.id"
                    @click="openPriceModal(row)"
                  >
                    调价
                  </button>
                  <button
                    class="ghost-btn small-btn"
                    :disabled="actionLoadingId === row.id || row.status === 1"
                    @click="toggleStatus(row)"
                  >
                    {{ row.status === 2 ? '启用' : '停用' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loading" class="table-state">正在加载机位列表…</div>
        <div v-else-if="machines.length === 0" class="table-state">当前筛选条件下没有机位数据</div>
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
            <h3>{{ formMode === 'create' ? '新增机位' : '编辑机位' }}</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="submitting" @click="closeFormModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitMachineForm">
          <label class="field">
            <span>机位编号</span>
            <input
              v-model.trim="machineForm.code"
              class="input"
              :disabled="submitting"
              placeholder="例如 A-021"
            />
          </label>

          <label class="field">
            <span>单价（元/分钟）</span>
            <input
              v-model.trim="machineForm.pricePerMin"
              class="input"
              inputmode="decimal"
              :disabled="submitting"
              placeholder="例如 0.12"
            />
          </label>

          <label class="field">
            <span>状态</span>
            <select
              v-model="machineForm.status"
              class="select"
              :disabled="submitting || machineForm.lockStatus"
            >
              <option value="0">空闲</option>
              <option v-if="machineForm.lockStatus" value="1">使用中</option>
              <option value="2">停用</option>
            </select>
            <small v-if="machineForm.lockStatus" class="field-tip">使用中的机位不能直接改状态，请先结束当前订单。</small>
          </label>

          <label class="field">
            <span>机位模板</span>
            <select v-model="machineForm.templateId" class="select" :disabled="submitting || templateLoading"><option value="">不绑定模板</option><option v-for="template in templates" :key="template.id" :value="String(template.id)">{{ template.name }}</option></select>
          </label>

          <label class="field">
            <span>所在区域</span>
            <input
              v-model.trim="machineForm.zone"
              class="input"
              :disabled="submitting"
              placeholder="例如 A区"
            />
          </label>

          <label class="field field-span-2">
            <span>配置摘要</span>
            <input
              v-model.trim="machineForm.spec"
              class="input"
              :disabled="submitting"
              placeholder="例如 i7 / RTX 4070 / 32G"
            />
          </label>

          <label class="field field-span-2">
            <span>最近维护时间</span>
            <input
              v-model="machineForm.lastMaintainedAt"
              class="input"
              type="datetime-local"
              :disabled="submitting"
            />
          </label>

          <p v-if="formError" class="form-error field-span-2">{{ formError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closeFormModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : formMode === 'create' ? '创建机位' : '保存修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showPriceModal" class="modal-overlay" @click.self="closePriceModal">
      <div class="modal-card modal-card-sm">
        <div class="modal-header">
          <div>
            <p class="eyebrow">价格</p>
            <h3>调整机位单价</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="submitting" @click="closePriceModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitPriceForm">
          <label class="field field-span-2">
            <span>机位编号</span>
            <input :value="priceForm.code" class="input" disabled />
          </label>

          <label class="field field-span-2">
            <span>新单价（元/分钟）</span>
            <input
              v-model.trim="priceForm.pricePerMin"
              class="input"
              inputmode="decimal"
              :disabled="submitting"
              placeholder="请输入新单价"
            />
          </label>

          <p v-if="priceError" class="form-error field-span-2">{{ priceError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closePriceModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : '确认调价' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showBatchModal" class="modal-overlay" @click.self="closeBatchModal">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <p class="eyebrow">模板</p>
            <h3>批量建机位</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="submitting" @click="closeBatchModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitBatchForm">
          <label class="field field-span-2">
            <span>机位模板</span>
            <select v-model="batchForm.templateId" class="select" :disabled="submitting || templateLoading">
              <option value="">请选择模板</option>
              <option v-for="template in templates" :key="template.id" :value="String(template.id)">
                {{ template.name }}
              </option>
            </select>
            <small v-if="selectedTemplateSummary" class="field-tip">模板摘要：{{ selectedTemplateSummary }}</small>
          </label>

          <label class="field">
            <span>编号前缀</span>
            <input v-model.trim="batchForm.codePrefix" class="input" :disabled="submitting" placeholder="例如 A-" />
          </label>

          <label class="field">
            <span>起始编号</span>
            <input v-model.trim="batchForm.startNo" class="input" inputmode="numeric" :disabled="submitting" />
          </label>

          <label class="field">
            <span>生成数量</span>
            <input v-model.trim="batchForm.count" class="input" inputmode="numeric" :disabled="submitting" />
          </label>

          <label class="field">
            <span>编号宽度</span>
            <input v-model.trim="batchForm.codeWidth" class="input" inputmode="numeric" :disabled="submitting" />
          </label>

          <label class="field field-span-2">
            <span>统一单价（元/分钟）</span>
            <input
              v-model.trim="batchForm.pricePerMin"
              class="input"
              inputmode="decimal"
              :disabled="submitting"
              placeholder="例如 0.10"
            />
          </label>

          <p v-if="batchError" class="form-error field-span-2">{{ batchError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submitting" @click="closeBatchModal">取消</button>
            <button class="solid-btn" type="submit" :disabled="submitting || templateLoading">
              {{ submitting ? '提交中…' : '开始生成' }}
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
import { Roles, getCurrentRole } from '../../../access'
import { requestJson, withQuery } from '../../../access/http'
import {
  type MachineItem,
  type MachineTemplate,
  type NoticeState,
  type PagedResponse,
  buildMachineConfigJson,
  formatCurrency,
  formatDate,
  formatDateTime,
  formatMinutes,
  parseMachineConfig,
  parseNumberInput,
  readErrorMessage,
  toDateTimeLocalValue,
  trimToUndefined
} from './shared'

const defaultMachineForm = () => ({
  id: 0,
  code: '',
  status: '0',
  pricePerMin: '0.10',
  templateId: '',
  zone: '',
  spec: '',
  lastMaintainedAt: '',
  lockStatus: false
})

const defaultBatchForm = () => ({
  templateId: '',
  codePrefix: '',
  startNo: '1',
  count: '10',
  codeWidth: '3',
  pricePerMin: '0.10'
})

const defaultFilters = () => ({
  keyword: '',
  status: '',
  minPrice: '',
  maxPrice: ''
})
const filters = reactive(defaultFilters())
const machines = ref<MachineItem[]>([])
const templates = ref<MachineTemplate[]>([])
const loading = ref(false)
const submitting = ref(false)
const templateLoading = ref(false)
const actionLoadingId = ref<number | null>(null)
const notice = reactive<NoticeState>({ type: 'success', message: '' })

const total = ref(0)
const page = ref(1)
const size = ref(10)

const showFormModal = ref(false)
const showPriceModal = ref(false)
const showBatchModal = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const machineForm = reactive(defaultMachineForm())
const priceForm = reactive({
  id: 0,
  code: '',
  pricePerMin: ''
})
const batchForm = reactive(defaultBatchForm())

const formError = ref('')
const priceError = ref('')
const batchError = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const isSuperAdmin = computed(() => getCurrentRole() === Roles.SUPER_ADMIN)

const selectedTemplateSummary = computed(() => {
  const templateId = Number(batchForm.templateId)
  if (!templateId) {
    return ''
  }
  const template = templates.value.find((item) => item.id === templateId)
  return template ? parseMachineConfig(template.configJson).spec || '已选择模板' : ''
})

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const clearNotice = () => {
  notice.message = ''
}

const resolveZone = (row: MachineItem) => parseMachineConfig(row.configJson).zone

const fetchTemplates = async () => {
  templateLoading.value = true
  try {
    templates.value = await requestJson<MachineTemplate[]>('/machine-templates')
  } finally {
    templateLoading.value = false
  }
}

const fetchMachines = async (targetPage = page.value) => {
  loading.value = true
  clearNotice()
  try {
    const data = await requestJson<PagedResponse<MachineItem>>(
      withQuery('/machines', {
        keyword: trimToUndefined(filters.keyword),
        status: filters.status,
        minPrice: parseNumberInput(filters.minPrice),
        maxPrice: parseNumberInput(filters.maxPrice),
        page: targetPage,
        size: size.value
      })
    )
    machines.value = data.items
    total.value = data.total
    page.value = data.page
    size.value = data.size
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载机位列表失败'))
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  void fetchMachines(1)
}

const resetFilters = () => {
  Object.assign(filters, defaultFilters())
  void fetchMachines(1)
}

const changePage = (targetPage: number) => {
  if (targetPage < 1 || targetPage > totalPages.value || targetPage === page.value) {
    return
  }
  void fetchMachines(targetPage)
}

const resetMachineForm = () => {
  Object.assign(machineForm, defaultMachineForm())
  formError.value = ''
}

const openCreateModal = () => {
  formMode.value = 'create'
  resetMachineForm()
  showFormModal.value = true
}

const openEditModal = async (id: number) => {
  actionLoadingId.value = id
  formError.value = ''
  try {
    const detail = await requestJson<MachineItem>(`/machines/${id}`)
    const config = parseMachineConfig(detail.configJson)
    Object.assign(machineForm, {
      id: detail.id,
      code: detail.code,
      status: String(detail.status),
      pricePerMin: detail.pricePerMin ? String(detail.pricePerMin) : '',
      templateId: detail.templateId ? String(detail.templateId) : '',
      zone: config.zone,
      spec: config.spec,
      lastMaintainedAt: toDateTimeLocalValue(detail.lastMaintainedAt),
      lockStatus: detail.status === 1
    })
    formMode.value = 'edit'
    showFormModal.value = true
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载机位详情失败'))
  } finally {
    actionLoadingId.value = null
  }
}

const closeFormModal = () => {
  if (submitting.value) {
    return
  }
  showFormModal.value = false
  resetMachineForm()
}

const validateMachineForm = () => {
  if (!machineForm.code.trim()) {
    return '请输入机位编号'
  }
  const price = parseNumberInput(machineForm.pricePerMin)
  if (!price || price <= 0) {
    return '单价必须大于 0'
  }
  return ''
}

const submitMachineForm = async () => {
  formError.value = validateMachineForm()
  if (formError.value) {
    return
  }

  submitting.value = true
  clearNotice()
  try {
    const payload: Record<string, unknown> = {
      code: machineForm.code.trim(),
      pricePerMin: parseNumberInput(machineForm.pricePerMin),
      templateId: machineForm.templateId ? Number(machineForm.templateId) : null,
      configJson: buildMachineConfigJson(machineForm.zone, machineForm.spec),
      lastMaintainedAt: machineForm.lastMaintainedAt || null
    }

    if (!(formMode.value === 'edit' && machineForm.lockStatus)) {
      payload.status = Number(machineForm.status)
    }

    if (formMode.value === 'create') {
      await requestJson<boolean>('/machines', {
        method: 'POST',
        body: JSON.stringify(payload)
      })
      setNotice('success', '机位已创建')
      showFormModal.value = false
      resetMachineForm()
      await fetchMachines(1)
    } else {
      await requestJson<boolean>(`/machines/${machineForm.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
      setNotice('success', `机位 ${machineForm.code} 已更新`)
      showFormModal.value = false
      resetMachineForm()
      await fetchMachines(page.value)
    }
  } catch (error) {
    formError.value = readErrorMessage(error, '提交机位信息失败')
  } finally {
    submitting.value = false
  }
}

const openPriceModal = (row: MachineItem) => {
  priceForm.id = row.id
  priceForm.code = row.code
  priceForm.pricePerMin = row.pricePerMin ? String(row.pricePerMin) : ''
  priceError.value = ''
  showPriceModal.value = true
}

const closePriceModal = () => {
  if (submitting.value) {
    return
  }
  showPriceModal.value = false
  priceForm.id = 0
  priceForm.code = ''
  priceForm.pricePerMin = ''
  priceError.value = ''
}

const submitPriceForm = async () => {
  const price = parseNumberInput(priceForm.pricePerMin)
  if (!price || price <= 0) {
    priceError.value = '请输入大于 0 的单价'
    return
  }

  submitting.value = true
  clearNotice()
  try {
    await requestJson<boolean>(`/machines/${priceForm.id}/price`, {
      method: 'PUT',
      body: JSON.stringify({ pricePerMin: price })
    })
    setNotice('success', `机位 ${priceForm.code} 已调价`)
    showPriceModal.value = false
    priceForm.id = 0
    priceForm.code = ''
    priceForm.pricePerMin = ''
    priceError.value = ''
    await fetchMachines(page.value)
  } catch (error) {
    priceError.value = readErrorMessage(error, '调价失败')
  } finally {
    submitting.value = false
  }
}

const openBatchModal = async () => {
  batchError.value = ''
  if (templates.value.length === 0) {
    try {
      await fetchTemplates()
    } catch (error) {
      setNotice('error', readErrorMessage(error, '加载机位模板失败'))
      return
    }
  }
  Object.assign(batchForm, defaultBatchForm())
  showBatchModal.value = true
}

const closeBatchModal = () => {
  if (submitting.value) {
    return
  }
  showBatchModal.value = false
  Object.assign(batchForm, defaultBatchForm())
  batchError.value = ''
}

const submitBatchForm = async () => {
  const templateId = Number(batchForm.templateId)
  const startNo = Number(batchForm.startNo)
  const count = Number(batchForm.count)
  const codeWidth = Number(batchForm.codeWidth)
  const pricePerMin = parseNumberInput(batchForm.pricePerMin)

  if (!templateId) {
    batchError.value = '请选择机位模板'
    return
  }
  if (!batchForm.codePrefix.trim()) {
    batchError.value = '请输入编号前缀'
    return
  }
  if (!Number.isInteger(startNo) || startNo <= 0) {
    batchError.value = '起始编号必须为正整数'
    return
  }
  if (!Number.isInteger(count) || count <= 0 || count > 100) {
    batchError.value = '生成数量需在 1 到 100 之间'
    return
  }
  if (!Number.isInteger(codeWidth) || codeWidth < 1 || codeWidth > 6) {
    batchError.value = '编号宽度需在 1 到 6 之间'
    return
  }
  if (!pricePerMin || pricePerMin <= 0) {
    batchError.value = '统一单价必须大于 0'
    return
  }

  submitting.value = true
  clearNotice()
  try {
    await requestJson<boolean>('/machines/batch-create', {
      method: 'POST',
      body: JSON.stringify({
        templateId,
        codePrefix: batchForm.codePrefix.trim(),
        startNo,
        count,
        codeWidth,
        pricePerMin
      })
    })
    setNotice('success', `已按模板批量创建 ${count} 台机位`)
    showBatchModal.value = false
    Object.assign(batchForm, defaultBatchForm())
    batchError.value = ''
    await fetchMachines(1)
  } catch (error) {
    batchError.value = readErrorMessage(error, '批量建机位失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: MachineItem) => {
  if (row.status === 1) {
    return
  }
  const nextStatus = row.status === 2 ? 0 : 2
  const actionLabel = nextStatus === 0 ? '启用' : '停用'
  if (!window.confirm(`确认${actionLabel}机位 ${row.code} 吗？`)) {
    return
  }

  actionLoadingId.value = row.id
  clearNotice()
  try {
    await requestJson<boolean>(`/machines/${row.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: nextStatus })
    })
    setNotice('success', `已${actionLabel}机位 ${row.code}`)
    await fetchMachines(page.value)
  } catch (error) {
    setNotice('error', readErrorMessage(error, `${actionLabel}机位失败`))
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(async () => {
  try {
    await fetchTemplates()
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载机位模板失败'))
  }
  await fetchMachines()
})
</script>

<style scoped>
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

.input-sm {
  max-width: 120px;
}

.machine-summary {
  display: grid;
  gap: 4px;
}

.machine-summary strong {
  color: #27231d;
  font-size: 14px;
  font-weight: 700;
}

.machine-summary span {
  color: #7d7568;
  font-size: 12px;
}

.field-tip {
  color: #8d806e;
  font-size: 12px;
  line-height: 1.4;
}
</style>
