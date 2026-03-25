import { clearKioskSession, getKioskToken } from './kiosk'

type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
}

export class ApiError extends Error {
  status: number
  code?: number

  constructor(message: string, status: number, code?: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

const apiBase = (import.meta.env.VITE_API_BASE ?? '').replace(/\/$/, '')

const buildUrl = (path: string) => `${apiBase}${path}`

const buildHeaders = (headers?: HeadersInit, hasBody = false) => {
  const nextHeaders = new Headers(headers)
  if (hasBody && !nextHeaders.has('Content-Type')) {
    nextHeaders.set('Content-Type', 'application/json')
  }
  const token = getKioskToken()
  if (token && !nextHeaders.has('Authorization')) {
    nextHeaders.set('Authorization', `Bearer ${token}`)
  }
  return nextHeaders
}

const parsePayload = async <T>(response: Response) => {
  const text = await response.text()
  if (!text) {
    return null
  }
  try {
    return JSON.parse(text) as ApiEnvelope<T>
  } catch {
    throw new ApiError('接口返回了无法解析的数据', response.status)
  }
}

export const requestJson = async <T>(path: string, init: RequestInit = {}) => {
  const hasBody = init.body !== undefined && init.body !== null
  const response = await fetch(buildUrl(path), {
    ...init,
    headers: buildHeaders(init.headers, hasBody)
  })
  const payload = await parsePayload<T>(response)

  if (!response.ok) {
    if (response.status === 401) {
      clearKioskSession()
    }
    throw new ApiError(payload?.message || `请求失败（HTTP ${response.status}）`, response.status, payload?.code)
  }

  if (!payload) {
    throw new ApiError('接口未返回业务数据', response.status)
  }

  if (payload.code !== 0) {
    if (payload.code === 401 || payload.code === 40101) {
      clearKioskSession()
    }
    throw new ApiError(payload.message || '接口调用失败', response.status, payload.code)
  }

  return payload.data
}
