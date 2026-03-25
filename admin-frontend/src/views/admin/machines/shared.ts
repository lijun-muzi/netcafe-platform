import { ApiError } from '../../../access/http'

export type NoticeState = {
  type: 'success' | 'error'
  message: string
}

export type PagedResponse<T> = {
  total: number
  page: number
  size: number
  items: T[]
}

export type MachineItem = {
  id: number
  code: string
  status: 0 | 1 | 2
  statusLabel: string
  statusTone: 'idle' | 'using' | 'disabled'
  pricePerMin: number
  priceLabel: string
  configJson: string | null
  configSummary: string
  templateId: number | null
  lastMaintainedAt: string | null
  createdAt: string | null
  updatedAt: string | null
  currentSessionId: number | null
  currentUserId: number | null
  currentUserName: string | null
  currentStartTime: string | null
  currentDurationMinutes: number | null
  currentFee: number | null
}

export type MachineTemplate = {
  id: number
  name: string
  configJson: string | null
  createdAt: string | null
  updatedAt: string | null
}

export type SessionItem = {
  id: number
  userId: number
  userName: string | null
  userMobile: string | null
  userBalance: number
  machineId: number
  machineCode: string | null
  startTime: string | null
  endTime: string | null
  durationMinutes: number | null
  billedMinutes: number | null
  priceSnapshot: number | null
  currentFee: number | null
  amount: number | null
  status: 0 | 1 | 2
  statusLabel: string
  statusTone: 'idle' | 'using' | 'forced'
  forceByAdminId: number | null
}

export type UserLookupItem = {
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

export type ParsedMachineConfig = {
  zone: string
  spec: string
}

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
})

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
})

const currencyFormatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

export const readErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof ApiError) {
    return error.message || fallback
  }
  if (error instanceof Error) {
    return error.message || fallback
  }
  return fallback
}

export const formatDateTime = (value: string | null) => {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return dateTimeFormatter.format(date)
}

export const formatDate = (value: string | null) => {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return dateFormatter.format(date)
}

export const formatCurrency = (value: number | string | null | undefined) => {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return '—'
  }
  return currencyFormatter.format(amount)
}

export const formatMinutes = (value: number | null | undefined) => {
  if (!Number.isFinite(Number(value))) {
    return '—'
  }
  return `${Number(value)} 分钟`
}

export const parseMachineConfig = (configJson: string | null | undefined): ParsedMachineConfig => {
  if (!configJson) {
    return { zone: '', spec: '' }
  }
  try {
    const parsed = JSON.parse(configJson) as Record<string, unknown>
    const spec = typeof parsed.spec === 'string' && parsed.spec.trim()
      ? parsed.spec.trim()
      : [parsed.cpu, parsed.gpu, parsed.memory]
        .filter((part): part is string => typeof part === 'string' && part.trim().length > 0)
        .map((part) => part.trim())
        .join(' / ')
    const zone = typeof parsed.zone === 'string' ? parsed.zone.trim() : ''
    return {
      zone,
      spec
    }
  } catch {
    return { zone: '', spec: configJson }
  }
}

export const buildMachineConfigJson = (zone: string, spec: string) => {
  const payload: Record<string, string> = {}
  if (zone.trim()) {
    payload.zone = zone.trim()
  }
  if (spec.trim()) {
    payload.spec = spec.trim()
  }
  return Object.keys(payload).length > 0 ? JSON.stringify(payload) : null
}

export const toDateTimeLocalValue = (value: string | null | undefined) => {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value.slice(0, 16)
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hour}:${minute}`
}

export const trimToUndefined = (value: string) => {
  const next = value.trim()
  return next ? next : undefined
}

export const parseNumberInput = (value: string) => {
  if (!value.trim()) {
    return undefined
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : undefined
}
