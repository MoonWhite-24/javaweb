import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/products', name: 'Products', component: () => import('../views/product/List.vue') },
  { path: '/products/:id', name: 'ProductDetail', component: () => import('../views/product/Detail.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/user/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/user/Register.vue') },
  { path: '/profile', name: 'Profile', component: () => import('../views/user/Profile.vue'), meta: { requiresAuth: true } },
  { path: '/cart', name: 'Cart', component: () => import('../views/cart/Index.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'Orders', component: () => import('../views/order/List.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:orderNo', name: 'OrderDetail', component: () => import('../views/order/Detail.vue'), meta: { requiresAuth: true } },
  { path: '/orders/pay/:orderNo', name: 'PayResult', component: () => import('../views/order/PayResult.vue'), meta: { requiresAuth: true } },
  { path: '/seckill', name: 'Seckill', component: () => import('../views/seckill/List.vue') },
  { path: '/seckill/result/:id', name: 'SeckillResult', component: () => import('../views/seckill/Result.vue'), meta: { requiresAuth: true } },
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/Login.vue') },
  { path: '/admin', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/products', name: 'AdminProducts', component: () => import('../views/admin/ProductList.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/products/:id/edit', name: 'AdminProductEdit', component: () => import('../views/admin/ProductEdit.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/orders', name: 'AdminOrders', component: () => import('../views/admin/OrderList.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/orders/:orderNo', name: 'AdminOrderDetail', component: () => import('../views/admin/OrderDetail.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('../views/admin/UserList.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/seckill', name: 'AdminSeckill', component: () => import('../views/admin/SeckillList.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/stats', name: 'AdminStats', component: () => import('../views/admin/Stats.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/error/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  if (to.meta.requiresAuth && !token) return next('/login')
  if (to.meta.requiresAdmin && (!userInfo || userInfo.role !== 1)) return next('/')
  next()
})

export default router
