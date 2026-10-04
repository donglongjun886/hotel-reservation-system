import request from './request'

// 金额口径：amount 为"分"整数，组件持分、仅在模板渲染处用 formatYuan 格式化（utils/money.js）
export const ORDER_STATUS = {
  CONFIRMED: { text: '已确认', type: 'primary' },
  CHECKED_IN: { text: '已入住', type: 'success' },
  COMPLETED: { text: '已完成', type: 'info' },
  CANCELLED: { text: '已取消', type: 'danger' }
}

export function createOrder(payload) {
  return request.post('/orders', payload)
}

// 我的订单（分页）：返回 { list, total, page, pageSize }
export function listMyOrders(page = 1, pageSize = 10) {
  return request.get('/orders/mine', { params: { page, pageSize } })
}

export function getMyOrder(orderNo) {
  return request.get(`/orders/${orderNo}`)
}

export function cancelOrder(orderNo) {
  return request.post(`/orders/${orderNo}/cancel`)
}
