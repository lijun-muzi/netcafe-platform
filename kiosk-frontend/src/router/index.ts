import { createRouter, createWebHistory } from 'vue-router'
import { getStoredMachineCode, hasKioskToken, setStoredMachineCode } from '../access/kiosk'
import { buildMachineQuery, readMachineQuery } from '../kiosk'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: (to) => ({
        name: 'kiosk-login',
        query: to.query
      })
    },
    {
      path: '/',
      component: () => import('../layouts/KioskLayout.vue'),
      children: [
        {
          path: 'login',
          name: 'kiosk-login',
          component: () => import('../views/Login.vue')
        },
        {
          path: 'status',
          name: 'kiosk-status',
          component: () => import('../views/Status.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'checkout',
          name: 'kiosk-checkout',
          component: () => import('../views/Checkout.vue'),
          meta: { requiresAuth: true }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const routeMachineCode = readMachineQuery(to.query.machine)
  const storedMachineCode = getStoredMachineCode() ?? ''
  const machineCode = routeMachineCode || storedMachineCode

  if (routeMachineCode) {
    setStoredMachineCode(routeMachineCode)
  }

  if (machineCode && routeMachineCode !== machineCode) {
    return {
      path: to.path,
      query: {
        ...to.query,
        ...buildMachineQuery(machineCode)
      },
      hash: to.hash,
      replace: true
    }
  }

  if (to.meta.requiresAuth && !hasKioskToken()) {
    return {
      name: 'kiosk-login',
      query: buildMachineQuery(machineCode),
      replace: true
    }
  }

  if (to.name === 'kiosk-login' && hasKioskToken()) {
    return {
      name: 'kiosk-status',
      query: buildMachineQuery(machineCode),
      replace: true
    }
  }

  return true
})

export default router
