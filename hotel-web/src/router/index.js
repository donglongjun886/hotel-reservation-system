import { createRouter, createWebHashHistory } from 'vue-router'
import { session } from '../stores/session'

const routes = [
  { path: '/', name: 'room-list', component: () => import('../views/guest/RoomListView.vue') },
  { path: '/room/:id', name: 'room-detail', component: () => import('../views/guest/RoomDetailView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/guest/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('../views/guest/RegisterView.vue') },
  { path: '/booking/confirm', name: 'booking-confirm', component: () => import('../views/guest/BookingConfirmView.vue'), meta: { requiresAuth: true } },
  { path: '/booking/result', name: 'booking-result', component: () => import('../views/guest/BookingResultView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'my-orders', component: () => import('../views/guest/MyOrdersView.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:orderNo', name: 'order-detail', component: () => import('../views/guest/OrderDetailView.vue'), meta: { requiresAuth: true } },
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/admin/AdminLoginView.vue') },
  { path: '/admin/orders', name: 'admin-orders', component: () => import('../views/admin/AdminOrdersView.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/orders/:orderNo', name: 'admin-order-detail', component: () => import('../views/admin/AdminOrderDetailView.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/room-types', name: 'admin-room-types', component: () => import('../views/admin/AdminRoomTypesView.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/rooms', name: 'admin-rooms', component: () => import('../views/admin/AdminRoomsView.vue'), meta: { requiresAdmin: true } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.requiresAdmin && (!session.user || session.user.role !== 'ADMIN')) {
    return { path: '/admin/login' }
  }
  if (to.meta.requiresAuth && !session.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
