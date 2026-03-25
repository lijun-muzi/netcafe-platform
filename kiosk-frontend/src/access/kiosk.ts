import type { KioskLoginResponse, KioskUserProfile } from '../kiosk'

const TOKEN_KEY = 'netcafe:kiosk:token'
const MACHINE_KEY = 'netcafe:kiosk:machine'
const USER_KEY = 'netcafe:kiosk:user'

const readStorage = (key: string) => {
  if (typeof window === 'undefined') {
    return null
  }
  return window.localStorage.getItem(key)
}

const writeStorage = (key: string, value: string) => {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.setItem(key, value)
}

const removeStorage = (key: string) => {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.removeItem(key)
}

export const getKioskToken = () => readStorage(TOKEN_KEY)

export const hasKioskToken = () => Boolean(getKioskToken())

export const getStoredMachineCode = () => readStorage(MACHINE_KEY)

export const setStoredMachineCode = (machineCode: string) => {
  writeStorage(MACHINE_KEY, machineCode)
}

export const getKioskUserProfile = () => {
  const raw = readStorage(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as KioskUserProfile
  } catch {
    removeStorage(USER_KEY)
    return null
  }
}

export const setKioskSession = (payload: Pick<KioskLoginResponse, 'token' | 'user'>) => {
  writeStorage(TOKEN_KEY, payload.token)
  writeStorage(USER_KEY, JSON.stringify(payload.user))
}

export const clearKioskSession = () => {
  removeStorage(TOKEN_KEY)
  removeStorage(USER_KEY)
}
