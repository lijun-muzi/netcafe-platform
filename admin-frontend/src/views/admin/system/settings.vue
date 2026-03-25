<template>
  <section class="page">
    <AppTopbar
      title="系统设置"
      subtitle="计费配置与机位模板"
      eyebrow="设置"
      action-label="刷新列表"
      :action-disabled="loadingConfig || loadingTemplates || submittingConfig || submittingTemplate"
      :action-handler="refreshAll"
    />

    <div v-if="notice.message" class="notice" :class="notice.type === 'error' ? 'notice-error' : 'notice-success'">
      {{ notice.message }}
    </div>

    <div class="card">
      <div class="card-header">
        <div>
          <p class="eyebrow">基础参数</p>
          <h3>默认单价与提醒阈值</h3>
        </div>
        <button class="solid-btn" :disabled="loadingConfig || submittingConfig" @click="submitConfig">
          {{ submittingConfig ? '保存中…' : '保存配置' }}
        </button>
      </div>

      <div class="config-grid">
        <label class="field">
          <span>默认单价（元 / 分钟）</span>
          <input
            v-model.trim="configForm.defaultPricePerMin"
            class="input"
            type="number"
            min="0.0001"
            step="0.01"
            :disabled="loadingConfig || submittingConfig"
            placeholder="例如 0.10"
          />
        </label>

        <label class="field">
          <span>低余额提醒阈值（分钟）</span>
          <input
            v-model.trim="configForm.lowBalanceThresholdMinutes"
            class="input"
            type="number"
            min="1"
            step="1"
            :disabled="loadingConfig || submittingConfig"
            placeholder="例如 60"
          />
        </label>
      </div>

      <div class="record-grid config-summary-grid">
        <div class="record-card">
          <p class="eyebrow">每分钟单价</p>
          <strong>{{ configPriceSummary }}</strong>
          <span>统一作为新机位默认价格</span>
        </div>
        <div class="record-card">
          <p class="eyebrow">每小时参考</p>
          <strong>{{ configHourlySummary }}</strong>
          <span>便于按小时感知整体计费水平</span>
        </div>
        <div class="record-card">
          <p class="eyebrow">余额提醒阈值</p>
          <strong>{{ configThresholdSummary }}</strong>
          <span>低于该剩余分钟数触发余额提醒</span>
        </div>
      </div>

      <p v-if="configError" class="form-error">{{ configError }}</p>
    </div>

    <div class="card page-card">
      <div class="card-header">
        <div>
          <p class="eyebrow">机位模板</p>
          <h3>常用配置模板</h3>
        </div>
        <button class="solid-btn" :disabled="submittingTemplate" @click="openCreateTemplateModal">新增模板</button>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>模板名称</th>
              <th>配置摘要</th>
              <th>创建时间</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="templates.length > 0">
            <tr v-for="row in templates" :key="row.id">
              <td>{{ row.name }}</td>
              <td>
                <div class="template-summary">
                  <strong>{{ row.configSummary || summarizeTemplateConfig(row.configJson) }}</strong>
                  <span>{{ row.configJson }}</span>
                </div>
              </td>
              <td>{{ formatDateTime(row.createdAt) }}</td>
              <td>{{ formatDateTime(row.updatedAt) }}</td>
              <td>
                <div class="table-actions">
                  <button
                    class="ghost-btn small-btn"
                    :disabled="templateActionLoadingId === row.id"
                    @click="openEditTemplateModal(row.id)"
                  >
                    编辑
                  </button>
                  <button
                    class="ghost-btn small-btn danger-btn"
                    :disabled="templateActionLoadingId === row.id"
                    @click="removeTemplate(row)"
                  >
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="loadingTemplates" class="table-state">正在加载机位模板…</div>
        <div v-else-if="templates.length === 0" class="table-state">当前没有机位模板</div>
      </div>
    </div>

    <div v-if="showTemplateModal" class="modal-overlay" @click.self="closeTemplateModal">
      <div class="modal-card modal-card-wide">
        <div class="modal-header">
          <div>
            <p class="eyebrow">{{ templateMode === 'create' ? '新增' : '编辑' }}</p>
            <h3>{{ templateMode === 'create' ? '新增机位模板' : '编辑机位模板' }}</h3>
          </div>
          <button class="ghost-btn small-btn" :disabled="submittingTemplate" @click="closeTemplateModal">关闭</button>
        </div>

        <form class="form-grid" @submit.prevent="submitTemplate">
          <label class="field field-span-2">
            <span>模板名称</span>
            <input
              v-model.trim="templateForm.name"
              class="input"
              :disabled="submittingTemplate"
              placeholder="例如：电竞旗舰 / 大众畅玩"
            />
          </label>

          <label class="field field-span-2">
            <span>配置 JSON</span>
            <textarea
              v-model="templateForm.configJson"
              class="textarea"
              :disabled="submittingTemplate"
              placeholder='例如：{"zone":"A区","spec":"i7 / RTX 4070 / 32G / 240Hz"}'
            />
          </label>

          <div class="field field-span-2">
            <span>配置预览</span>
            <div class="preview-box">
              <strong>{{ templatePreviewSummary }}</strong>
              <pre>{{ templatePreviewJson }}</pre>
            </div>
          </div>

          <p v-if="templateError" class="form-error field-span-2">{{ templateError }}</p>

          <div class="modal-footer field-span-2">
            <button class="ghost-btn" type="button" :disabled="submittingTemplate" @click="closeTemplateModal">
              取消
            </button>
            <button class="solid-btn" type="submit" :disabled="submittingTemplate">
              {{ submittingTemplate ? '提交中…' : templateMode === 'create' ? '创建模板' : '保存修改' }}
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
import { requestJson } from '../../../access/http'
import {
  type NoticeState,
  formatCurrency,
  formatDateTime,
  prettyJson,
  readErrorMessage
} from '../shared'

type SystemConfig = {
  defaultPricePerMin: number
  lowBalanceThresholdMinutes: number
}

type MachineTemplateItem = {
  id: number
  name: string
  configJson: string
  configSummary: string
  createdAt: string | null
  updatedAt: string | null
}

const configForm = reactive({
  defaultPricePerMin: '',
  lowBalanceThresholdMinutes: ''
})

const templateForm = reactive({
  id: 0,
  name: '',
  configJson: ''
})

const templates = ref<MachineTemplateItem[]>([])
const loadingConfig = ref(false)
const loadingTemplates = ref(false)
const submittingConfig = ref(false)
const submittingTemplate = ref(false)
const templateActionLoadingId = ref<number | null>(null)
const notice = reactive<NoticeState>({ type: 'success', message: '' })
const configError = ref('')
const templateError = ref('')
const showTemplateModal = ref(false)
const templateMode = ref<'create' | 'edit'>('create')

const setNotice = (type: NoticeState['type'], message: string) => {
  notice.type = type
  notice.message = message
}

const clearNotice = () => {
  notice.message = ''
}

const summarizeTemplateConfig = (configJson: string | null | undefined) => {
  if (!configJson?.trim()) {
    return '未配置'
  }
  try {
    const parsed = JSON.parse(configJson) as Record<string, unknown>
    if (typeof parsed.spec === 'string' && parsed.spec.trim()) {
      return parsed.spec.trim()
    }
    const parts = [parsed.cpu, parsed.gpu, parsed.memory]
      .filter((part): part is string => typeof part === 'string' && part.trim().length > 0)
      .map((part) => part.trim())
    const summary = parts.length > 0 ? parts.join(' / ') : configJson.trim()
    if (typeof parsed.zone === 'string' && parsed.zone.trim()) {
      return `${parsed.zone.trim()} · ${summary}`
    }
    return summary
  } catch {
    return configJson.trim()
  }
}

const resetTemplateForm = () => {
  templateForm.id = 0
  templateForm.name = ''
  templateForm.configJson = ''
  templateError.value = ''
}

const configPriceSummary = computed(() => {
  const price = Number(configForm.defaultPricePerMin)
  return Number.isFinite(price) && price > 0 ? formatCurrency(price) : '—'
})

const configHourlySummary = computed(() => {
  const price = Number(configForm.defaultPricePerMin)
  return Number.isFinite(price) && price > 0 ? formatCurrency(price * 60) : '—'
})

const configThresholdSummary = computed(() => {
  const threshold = Number(configForm.lowBalanceThresholdMinutes)
  return Number.isFinite(threshold) && threshold > 0 ? `${threshold} 分钟` : '—'
})

const templatePreviewSummary = computed(() => summarizeTemplateConfig(templateForm.configJson))

const templatePreviewJson = computed(() => prettyJson(templateForm.configJson))

const fetchConfig = async () => {
  loadingConfig.value = true
  configError.value = ''
  clearNotice()
  try {
    const data = await requestJson<SystemConfig>('/system/config')
    configForm.defaultPricePerMin = String(data.defaultPricePerMin ?? '')
    configForm.lowBalanceThresholdMinutes = String(data.lowBalanceThresholdMinutes ?? '')
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载系统配置失败'))
  } finally {
    loadingConfig.value = false
  }
}

const fetchTemplates = async () => {
  loadingTemplates.value = true
  clearNotice()
  try {
    templates.value = await requestJson<MachineTemplateItem[]>('/machine-templates')
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载机位模板失败'))
  } finally {
    loadingTemplates.value = false
  }
}

const refreshAll = () => {
  void Promise.all([fetchConfig(), fetchTemplates()])
}

const validateConfigForm = () => {
  const price = Number(configForm.defaultPricePerMin)
  if (!Number.isFinite(price) || price <= 0) {
    return '默认单价必须大于 0'
  }
  const threshold = Number(configForm.lowBalanceThresholdMinutes)
  if (!Number.isInteger(threshold) || threshold <= 0) {
    return '余额提醒阈值必须是大于 0 的整数'
  }
  return ''
}

const submitConfig = async () => {
  configError.value = validateConfigForm()
  if (configError.value) {
    return
  }

  submittingConfig.value = true
  clearNotice()
  try {
    await requestJson<boolean>('/system/config', {
      method: 'PUT',
      body: JSON.stringify({
        defaultPricePerMin: Number(configForm.defaultPricePerMin),
        lowBalanceThresholdMinutes: Number(configForm.lowBalanceThresholdMinutes)
      })
    })
    setNotice('success', '系统配置已保存')
    await fetchConfig()
  } catch (error) {
    configError.value = readErrorMessage(error, '保存系统配置失败')
  } finally {
    submittingConfig.value = false
  }
}

const openCreateTemplateModal = () => {
  templateMode.value = 'create'
  resetTemplateForm()
  showTemplateModal.value = true
}

const openEditTemplateModal = async (id: number) => {
  templateActionLoadingId.value = id
  templateMode.value = 'edit'
  templateError.value = ''
  try {
    const detail = await requestJson<MachineTemplateItem>(`/machine-templates/${id}`)
    templateForm.id = detail.id
    templateForm.name = detail.name
    templateForm.configJson = detail.configJson || ''
    showTemplateModal.value = true
  } catch (error) {
    setNotice('error', readErrorMessage(error, '加载模板详情失败'))
  } finally {
    templateActionLoadingId.value = null
  }
}

const closeTemplateModal = () => {
  if (submittingTemplate.value) {
    return
  }
  showTemplateModal.value = false
  templateError.value = ''
}

const normalizeTemplateJson = (value: string) => {
  const trimmed = value.trim()
  if (!trimmed) {
    throw new Error('请填写配置 JSON')
  }
  const parsed = JSON.parse(trimmed)
  if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
    throw new Error('配置 JSON 必须是对象')
  }
  return JSON.stringify(parsed)
}

const validateTemplateForm = () => {
  if (!templateForm.name.trim()) {
    return '请输入模板名称'
  }
  try {
    normalizeTemplateJson(templateForm.configJson)
  } catch (error) {
    return error instanceof Error ? error.message : '配置 JSON 格式不正确'
  }
  return ''
}

const submitTemplate = async () => {
  templateError.value = validateTemplateForm()
  if (templateError.value) {
    return
  }

  submittingTemplate.value = true
  clearNotice()
  try {
    const payload = {
      name: templateForm.name.trim(),
      configJson: normalizeTemplateJson(templateForm.configJson)
    }
    if (templateMode.value === 'create') {
      await requestJson<boolean>('/machine-templates', {
        method: 'POST',
        body: JSON.stringify(payload)
      })
      setNotice('success', '机位模板已创建')
    } else {
      await requestJson<boolean>(`/machine-templates/${templateForm.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
      setNotice('success', '机位模板已更新')
    }
    showTemplateModal.value = false
    resetTemplateForm()
    await fetchTemplates()
  } catch (error) {
    templateError.value = readErrorMessage(error, templateMode.value === 'create' ? '创建模板失败' : '更新模板失败')
  } finally {
    submittingTemplate.value = false
  }
}

const removeTemplate = async (row: MachineTemplateItem) => {
  if (!window.confirm(`确认删除模板「${row.name}」吗？`)) {
    return
  }
  templateActionLoadingId.value = row.id
  clearNotice()
  try {
    await requestJson<boolean>(`/machine-templates/${row.id}`, {
      method: 'DELETE'
    })
    setNotice('success', `模板「${row.name}」已删除`)
    await fetchTemplates()
  } catch (error) {
    setNotice('error', readErrorMessage(error, '删除模板失败'))
  } finally {
    templateActionLoadingId.value = null
  }
}

onMounted(async () => {
  await Promise.all([fetchConfig(), fetchTemplates()])
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

.config-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.config-summary-grid {
  margin-top: 18px;
}

.record-card span {
  color: #7d7568;
  font-size: 12px;
  line-height: 1.45;
}

.table-wrap {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.template-summary {
  display: grid;
  gap: 4px;
}

.template-summary strong {
  color: #27231d;
  font-size: 14px;
}

.template-summary span {
  color: #7d7568;
  font-size: 12px;
  line-height: 1.45;
  word-break: break-all;
}

.textarea {
  min-height: 180px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.78);
  color: var(--ink);
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
}

.textarea:focus {
  outline: none;
  border-color: rgba(255, 138, 0, 0.45);
  box-shadow: 0 0 0 4px rgba(255, 138, 0, 0.12);
}

.preview-box {
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: rgba(255, 249, 241, 0.88);
  display: grid;
  gap: 10px;
}

.preview-box strong {
  color: #27231d;
  font-size: 15px;
}

.preview-box pre {
  overflow: auto;
  border-radius: 14px;
  background: rgba(23, 23, 23, 0.04);
  padding: 12px;
  color: #5f564a;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 960px) {
  .config-grid {
    grid-template-columns: 1fr;
  }
}
</style>
