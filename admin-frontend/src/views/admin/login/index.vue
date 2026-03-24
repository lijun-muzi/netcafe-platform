<template>
  <div class="login-shell">
    <div class="login-card reveal">
      <section class="brand-panel">
        <div class="brand-top">
          <div class="brand-chip">星河网咖</div>
          <div class="brand-branch">DRHL分店</div>
        </div>
        <h1>星河网咖运营控制台</h1>
        <p class="brand-subtitle">管理端登录入口</p>
        <div class="brand-rail">
          <div class="rail-item">
            <span class="rail-dot"></span>
            <p>机位管理与实时监控</p>
          </div>
          <div class="rail-item">
            <span class="rail-dot"></span>
            <p>用户档案与上机控制</p>
          </div>
          <div class="rail-item">
            <span class="rail-dot"></span>
            <p>运营数据与班次协同</p>
          </div>
        </div>
        <div class="brand-tags">
          <span>安全登录</span>
          <span>权限分级</span>
          <span>单店运营</span>
        </div>
        <div class="brand-contact">
          <div class="contact-item">
            <span class="contact-label">前台电话</span>
            <strong>13507489173</strong>
          </div>
          <div class="contact-item">
            <span class="contact-label">门店地址</span>
            <strong>湖南省长沙市芙蓉区xxx街道xx号星河网咖</strong>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <p class="eyebrow">管理员登录</p>
        <h2>进入运营控制台</h2>

        <form class="form-grid" @submit.prevent="handleLogin">
        <label>
          账号
          <input
            v-model.trim="username"
            class="input"
            placeholder="请输入账号"
            autocomplete="username"
            :disabled="loading"
          />
        </label>
        <label>
          密码
          <div class="input-wrap">
            <input
              v-model="password"
              class="input"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              autocomplete="current-password"
              :disabled="loading"
            />
            <button
              class="toggle-btn icon"
              type="button"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="togglePassword"
              :disabled="loading"
            >
              <svg v-if="showPassword" viewBox="0 0 24 24" aria-hidden="true">
                <path
                  d="M12 5c5.5 0 9.5 4.1 10.9 6.1a1.5 1.5 0 0 1 0 1.8C21.5 15 17.5 19 12 19S2.5 15 1.1 12.9a1.5 1.5 0 0 1 0-1.8C2.5 9.1 6.5 5 12 5Zm0 2.2c-3.7 0-6.8 2.5-8.4 4.7 1.6 2.2 4.7 4.7 8.4 4.7s6.8-2.5 8.4-4.7c-1.6-2.2-4.7-4.7-8.4-4.7Zm0 2a2.8 2.8 0 1 1 0 5.6 2.8 2.8 0 0 1 0-5.6Z"
                />
              </svg>
              <svg v-else viewBox="0 0 24 24" aria-hidden="true">
                <path
                  d="M4.3 3.2 3 4.5l3.2 3.1C4 8.9 2.5 10.5 1.1 12.1a1.5 1.5 0 0 0 0 1.8C2.5 16 6.5 20 12 20c2 0 3.8-.5 5.4-1.4l3.3 3.2 1.2-1.3L4.3 3.2ZM12 17.8c-3.6 0-6.7-2.5-8.4-4.7 1-1.3 2.6-2.9 4.6-3.9l2.1 2a2.8 2.8 0 0 0 3.8 3.8l2.1 2.1c-1.2.4-2.5.7-4.2.7Zm.2-8.4 2.9 2.8a2.8 2.8 0 0 0-2.9-2.8ZM12 4c2 0 3.8.5 5.4 1.4l-1.6 1.6A9.9 9.9 0 0 0 12 6.2c-1.7 0-3.2.4-4.6 1.1L5.8 5.7A11 11 0 0 1 12 4Zm8.4 8.1c-.8 1-1.9 2.2-3.3 3.2l-1.5-1.5a10 10 0 0 0 3.1-2.6c-1.6-2.2-4.7-4.7-8.4-4.7-.6 0-1.1 0-1.7.1l-1.7-1.7c1-.2 2.1-.3 3.4-.3 5.5 0 9.5 4.1 10.9 6.1a1.5 1.5 0 0 1 0 1.4Z"
                />
              </svg>
            </button>
          </div>
        </label>
          <div class="form-actions">
            <button class="solid-btn" type="submit" :disabled="loading">
              {{ loading ? '登录中…' : '登录' }}
            </button>
            <span v-if="errorMessage" class="form-error-inline">{{ errorMessage }}</span>
          </div>
        </form>

        <div class="login-hint">
          <span>今日安全提示</span>
          <p>连续 5 次失败将触发 3 分钟冻结。</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Roles, setAdminProfile, setAuthToken, setCurrentRole } from '../../../access'
import type { AdminProfile, Role } from '../../../access'

type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

type LoginResponse = {
  token: string
  tokenType?: string
  expiresIn?: number
  role?: Role
  admin?: AdminProfile
}

const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)

const apiBase = (import.meta.env.VITE_API_BASE ?? '').replace(/\/$/, '')
const loginUrl = `${apiBase}/auth/login`
const dashboardPath = '/admin/dashboard'

const resolveRedirectPath = (redirect?: string) => {
  if (!redirect || redirect === '/' || redirect === '/login' || redirect === '/admin/login') {
    return dashboardPath
  }
  return redirect
}

const normalizeRole = (role?: string): Role | null => {
  if (role === Roles.SUPER_ADMIN || role === Roles.ADMIN) {
    return role
  }
  return null
}

const handleLogin = async () => {
  errorMessage.value = ''
  if (!username.value || !password.value) {
    errorMessage.value = '请输入账号与密码'
    return
  }
  loading.value = true
  try {
    const response = await fetch(loginUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    })
    const payload = (await response.json().catch(() => null)) as ApiResponse<LoginResponse> | null
    if (!response.ok || !payload) {
      throw new Error('登录失败，请稍后重试')
    }
    if (payload.code !== 0) {
      throw new Error(payload.message || '账号或密码错误')
    }
    if (!payload.data?.token) {
      throw new Error('登录失败，未返回令牌')
    }
    setAuthToken(payload.data.token)
    const role = normalizeRole(payload.data.role || payload.data.admin?.role)
    if (role) {
      setCurrentRole(role)
    }
    if (payload.data.admin) {
      const profileRole = normalizeRole(payload.data.admin.role) || role || Roles.ADMIN
      setAdminProfile({ ...payload.data.admin, role: profileRole })
    }
    const redirect = Array.isArray(route.query.redirect) ? route.query.redirect[0] : route.query.redirect
    router.replace(resolveRedirectPath(redirect as string | undefined))
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  background:
    radial-gradient(circle at top left, rgba(255, 138, 0, 0.12), transparent 45%),
    radial-gradient(circle at 70% 10%, rgba(29, 154, 163, 0.12), transparent 45%),
    linear-gradient(180deg, rgba(23, 23, 23, 0.04), transparent 45%);
}

.login-card {
  width: min(860px, 100%);
  border-radius: 24px;
  background: #ffffff;
  box-shadow: 0 30px 60px rgba(23, 23, 23, 0.12);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  overflow: hidden;
}

.brand-panel {
  padding: 36px 32px;
  background: linear-gradient(140deg, rgba(255, 138, 0, 0.18), rgba(255, 255, 255, 0.9));
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: relative;
  overflow: hidden;
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 28px;
}

.brand-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(23, 23, 23, 0.08);
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--ink);
}

.brand-branch {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(23, 23, 23, 0.06);
  font-size: 12px;
  color: var(--muted);
}

.brand-panel h1 {
  font-family: var(--font-display);
  font-size: 32px;
  line-height: 1.1;
}

.brand-subtitle {
  color: var(--muted);
  font-size: 14px;
}

.brand-rail {
  display: grid;
  gap: 10px;
  margin-top: 4px;
}

.rail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--muted);
}

.rail-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(23, 23, 23, 0.3);
}

.brand-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.brand-tags span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(23, 23, 23, 0.06);
  font-size: 12px;
  color: var(--muted);
}

.brand-contact {
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px dashed rgba(23, 23, 23, 0.12);
  display: grid;
  gap: 8px;
  font-size: 12px;
  color: var(--muted);
}

.contact-item {
  display: grid;
  gap: 4px;
}

.contact-label {
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--muted);
}

.brand-contact strong {
  color: var(--ink);
  font-size: 13px;
  line-height: 1.4;
}


.form-panel {
  padding: 36px 32px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-panel .eyebrow {
  display: inline-flex;
  align-items: center;
  height: 28px;
}

.form-panel h2 {
  font-family: var(--font-display);
  font-size: 26px;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-grid label {
  font-size: 13px;
  color: var(--muted);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-wrap .input {
  width: 100%;
  padding-right: 44px;
}

.toggle-btn {
  position: absolute;
  right: 12px;
  height: 32px;
  width: 32px;
  border-radius: 999px;
  border: none;
  background: transparent;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-btn:hover {
  color: var(--ink);
}

.toggle-btn[disabled] {
  cursor: not-allowed;
  opacity: 0.6;
}

.toggle-btn svg {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.form-actions {
  margin-top: 4px;
  display: flex;
  align-items: stretch;
  gap: 12px;
}

.form-actions .solid-btn {
  height: 42px;
  padding: 0 20px;
}

.form-error-inline {
  flex: 1;
  height: 42px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid rgba(215, 44, 60, 0.2);
  background: rgba(215, 44, 60, 0.08);
  color: var(--red);
  font-size: 12px;
  white-space: nowrap;
  display: flex;
  align-items: center;
}

.solid-btn[disabled] {
  cursor: not-allowed;
  opacity: 0.7;
  box-shadow: none;
  transform: none;
}

.login-hint {
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--bg-alt);
  border: 1px solid var(--border);
  font-size: 12px;
  color: var(--muted);
  margin-top: auto;
}

:deep(.input[type='password'])::-ms-reveal,
:deep(.input[type='password'])::-ms-clear {
  display: none;
}

:deep(.input[type='password'])::-webkit-credentials-auto-fill-button {
  visibility: hidden;
  display: none;
}

@media (max-width: 720px) {
  .login-card {
    border-radius: 18px;
  }
  .brand-panel,
  .form-panel {
    padding: 28px 22px;
  }
  .brand-panel h1 {
    font-size: 26px;
  }
}
</style>
