export const Roles = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  ADMIN: 'ADMIN'
} as const

export type Role = (typeof Roles)[keyof typeof Roles]

export type AdminProfile = {
  id: number
  username: string
  name: string
  role: Role
}

const roleKey = 'netcafe-role'
const tokenKey = 'netcafe-token'
const profileKey = 'netcafe-profile'

const hasStorage = () => typeof localStorage !== 'undefined'

export const getCurrentRole = (): Role => {
  if (!hasStorage()) {
    return Roles.SUPER_ADMIN
  }
  const cached = localStorage.getItem(roleKey)
  return cached === Roles.ADMIN ? Roles.ADMIN : Roles.SUPER_ADMIN
}

export const setCurrentRole = (role: Role) => {
  if (!hasStorage()) {
    return
  }
  localStorage.setItem(roleKey, role)
}

export const getAuthToken = (): string | null => {
  if (!hasStorage()) {
    return null
  }
  return localStorage.getItem(tokenKey)
}

export const setAuthToken = (token: string) => {
  if (!hasStorage()) {
    return
  }
  localStorage.setItem(tokenKey, token)
}

export const getAdminProfile = (): AdminProfile | null => {
  if (!hasStorage()) {
    return null
  }
  const raw = localStorage.getItem(profileKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as AdminProfile
  } catch {
    return null
  }
}

export const setAdminProfile = (profile: AdminProfile) => {
  if (!hasStorage()) {
    return
  }
  localStorage.setItem(profileKey, JSON.stringify(profile))
}

export const clearAuth = () => {
  if (!hasStorage()) {
    return
  }
  localStorage.removeItem(tokenKey)
  localStorage.removeItem(profileKey)
  localStorage.removeItem(roleKey)
}

export const isAuthenticated = () => Boolean(getAuthToken())

export const canAccess = (roles?: Role[]) => {
  if (!roles || roles.length === 0) {
    return true
  }
  const currentRole = getCurrentRole()
  return roles.includes(currentRole)
}
