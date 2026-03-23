import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/',
      component: () => import('../layouts/KioskLayout.vue'),
      children: [
        { path: 'login', component: () => import('../views/Login.vue') },
        { path: 'status', component: () => import('../views/Status.vue') },
        { path: 'checkout', component: () => import('../views/Checkout.vue') }
      ]
    }
  ]
})

export default router
