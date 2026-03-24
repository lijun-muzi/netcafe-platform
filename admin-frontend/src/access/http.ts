import { clearAuth, getAuthToken } from './index'

type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
}

type QueryValue = string | number | boolean | null | undefined

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
  const token = getAuthToken()
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
      clearAuth()
    }
    throw new ApiError(payload?.message || `请求失败（HTTP ${response.status}）`, response.status, payload?.code)
  }

  if (!payload) {
    throw new ApiError('接口未返回业务数据', response.status)
  }

  if (payload.code !== 0) {
    if (payload.code === 401 || payload.code === 40101) {
      clearAuth()
    }
    throw new ApiError(payload.message || '接口调用失败', response.status, payload.code)
  }

  return payload.data
}

export const withQuery = (path: string, query: Record<string, QueryValue>) => {
  const params = new URLSearchParams()
  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    params.set(key, String(value))
  })
  const queryString = params.toString()
  return queryString ? `${path}?${queryString}` : path
}
