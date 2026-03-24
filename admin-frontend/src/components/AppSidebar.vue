<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <div class="brand-mark"></div>
      <div>
        <p class="brand-title">星河网咖</p>
        <span class="brand-sub">管理端 · MVP</span>
      </div>
    </div>

    <nav class="nav">
      <router-link v-for="item in visibleNav" :key="item.path" :to="item.path" class="nav-link">
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-text">{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="status-card profile-card">
        <div class="profile-top">
          <div class="profile-copy">
            <p class="profile-eyebrow">当前登录</p>
          </div>
          <div class="profile-avatar">{{ profileBadgeText }}</div>
        </div>
        <div class="profile-meta">
          <div class="profile-meta-item">
            <span class="profile-meta-label">账号</span>
            <strong class="profile-meta-value">{{ profile?.username || '—' }}</strong>
          </div>
          <div class="profile-meta-item">
            <span class="profile-meta-label">角色</span>
            <strong class="profile-meta-value">{{ roleLabel }}</strong>
          </div>
        </div>
      </div>
      <button class="ghost-btn full" @click="handleLogout">退出登录</button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Roles, clearAuth, getAdminProfile, getAuthToken, getCurrentRole } from '../access'

const router = useRouter()
const profile = computed(() => getAdminProfile())
const currentRole = computed(() => profile.value?.role || getCurrentRole())

const nav = [
  { path: '/admin/dashboard', label: '实时面板', icon: '01' },
  { path: '/admin/staff', label: '员工管理', icon: '02', roles: [Roles.SUPER_ADMIN] },
  { path: '/admin/users', label: '用户管理', icon: '03' },
  { path: '/admin/machines', label: '机位管理', icon: '04' },
  { path: '/admin/sessions', label: '上机管理', icon: '05' },
  { path: '/admin/orders', label: '订单与充值', icon: '06' },
  { path: '/admin/stats', label: '运营统计', icon: '07', roles: [Roles.SUPER_ADMIN] },
  { path: '/admin/settings', label: '系统设置', icon: '08', roles: [Roles.SUPER_ADMIN] },
  { path: '/admin/audit', label: '审计日志', icon: '09', roles: [Roles.SUPER_ADMIN] }
]

const visibleNav = computed(() => nav.filter((item) => !item.roles || item.roles.includes(currentRole.value)))

const roleLabel = computed(() => (currentRole.value === Roles.ADMIN ? '管理员' : '超级管理员'))
const profileBadgeText = computed(() => {
  const name = profile.value?.name?.trim()
  if (name) {
    return name
  }

  const username = profile.value?.username?.trim()
  if (!username) {
    return 'NC'
  }

  return username.slice(0, 2).toUpperCase()
})

const handleLogout = () => {
  void logoutAndExit()
}

const logoutAndExit = async () => {
  const token = getAuthToken()
  if (token) {
    const apiBase = (import.meta.env.VITE_API_BASE ?? '').replace(/\/$/, '')
    try {
      const response = await fetch(`${apiBase}/auth/logout`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
      if (!response.ok) {
        window.alert(`登出失败（状态码 ${response.status}），请稍后重试。`)
        return
      }
    } catch {
      window.alert('登出失败，网络异常或接口未就绪。')
      return
    }
  }
  clearAuth()
  router.replace('/admin/login')
}
</script>
