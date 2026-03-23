import { createRouter, createWebHistory } from 'vue-router'
import { Roles, canAccess, isAuthenticated } from '../access'
import type { Role } from '../access'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/admin/dashboard' },
    {
      path: '/admin/login',
      component: () => import('../views/admin/login/index.vue')
    },
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', component: () => import('../views/admin/dashboard/index.vue') },
        {
          path: 'staff',
          component: () => import('../views/admin/people/staff.vue'),
          meta: { roles: [Roles.SUPER_ADMIN] }
        },
        { path: 'users', component: () => import('../views/admin/people/users.vue') },
        { path: 'machines', component: () => import('../views/admin/machines/machines.vue') },
        { path: 'sessions', component: () => import('../views/admin/machines/sessions.vue') },
        { path: 'orders', component: () => import('../views/admin/finance/orders.vue') },
        {
          path: 'stats',
          component: () => import('../views/admin/finance/stats.vue'),
          meta: { roles: [Roles.SUPER_ADMIN] }
        },
        {
          path: 'settings',
          component: () => import('../views/admin/system/settings.vue'),
          meta: { roles: [Roles.SUPER_ADMIN] }
        },
        {
          path: 'audit',
          component: () => import('../views/admin/system/audit.vue'),
          meta: { roles: [Roles.SUPER_ADMIN] }
        },
        {
          path: 'forbidden',
          component: () => import('../views/admin/forbidden.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const isLogin = to.path === '/admin/login'
  const authed = isAuthenticated()
  if (!authed && !isLogin) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  if (authed && isLogin) {
    return { path: '/admin/dashboard' }
  }
  const roles = to.meta.roles as Role[] | undefined
  if (!roles) {
    return true
  }
  if (canAccess(roles)) {
    return true
  }
  return { path: '/admin/forbidden', query: { from: to.fullPath } }
})

export default router
