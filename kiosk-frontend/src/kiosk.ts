import { ApiError } from './access/http'

export type KioskUserProfile = {
  id: number
  name: string
  mobile: string
  idCard: string
}

export type KioskMachineOverview = {
  machineId: number
  machineCode: string
  status: number
  statusLabel: string
  available: boolean
  availabilityMessage: string
  pricePerMin: number
  priceLabel: string
  lowBalanceThresholdMinutes: number
}

export type KioskSessionStatus = {
  sessionId: number
  userId: number
  userName: string
  machineId: number
  machineCode: string
  startTime: string
  currentDurationMinutes: number
  currentDurationLabel: string
  billedMinutes: number
  pricePerMin: number
  priceLabel: string
  currentFee: number
  currentFeeLabel: string
  billedAmount: number
  billedAmountLabel: string
  balance: number
  balanceLabel: string
  remainingMinutes: number
  remainingMinutesLabel: string
  lowBalanceThresholdMinutes: number
  lowBalanceWarning: boolean
  lowBalanceMessage: string
  status: number
  statusLabel: string
  paused: boolean
  pausedAt: string | null
}

export type KioskCheckout = {
  sessionId: number
  userId: number
  userName: string
  machineCode: string
  status: number
  statusLabel: string
  startTime: string
  endTime: string
  durationMinutes: number
  durationLabel: string
  pricePerMin: number
  priceLabel: string
  totalAmount: number
  totalAmountLabel: string
  settlementAmount: number
  settlementAmountLabel: string
  balanceBefore: number
  balanceBeforeLabel: string
  balanceAfter: number
  balanceAfterLabel: string
}

export type KioskLoginResponse = {
  token: string
  tokenType: string
  expiresIn: number
  user: KioskUserProfile
  machine: KioskMachineOverview
  session: KioskSessionStatus
}

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
})

export const normalizeMachineCode = (value: string | null | undefined) => value?.trim().toUpperCase() ?? ''

export const readMachineQuery = (value: unknown) => {
  if (Array.isArray(value)) {
    return normalizeMachineCode(value[0])
  }
  if (typeof value === 'string') {
    return normalizeMachineCode(value)
  }
  return ''
}

export const buildMachineQuery = (machineCode: string) => {
  const normalized = normalizeMachineCode(machineCode)
  return normalized ? { machine: normalized } : {}
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

export const readErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof ApiError) {
    return error.message || fallback
  }
  if (error instanceof Error) {
    return error.message || fallback
  }
  return fallback
}
