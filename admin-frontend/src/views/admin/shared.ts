import { ApiError } from '../../access/http'

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

export type SelectOption = {
  value: string
  label: string
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

export const formatDateTime = (value: string | null | undefined) => {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return dateTimeFormatter.format(date)
}

export const formatDate = (value: string | null | undefined) => {
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

export const trimToUndefined = (value: string) => {
  const next = value.trim()
  return next ? next : undefined
}

export const prettyJson = (value: string | null | undefined) => {
  if (!value) {
    return '—'
  }
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}
