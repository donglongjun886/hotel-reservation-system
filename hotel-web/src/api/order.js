import request from './request'

// 金额口径：接口传"分"整数，分→元换算只允许在 api 层，组件拿到的就是元
function toYuan(fen) {
  return fen / 100
}

function convertOrder(info) {
  return { ...info, amount: toYuan(info.amount) }
}

export const ORDER_STATUS = {
  CONFIRMED: { text: '已确认', type: 'primary' },
  CHECKED_IN: { text: '已入住', type: 'success' },
  COMPLETED: { text: '已完成', type: 'info' },
  CANCELLED: { text: '已取消', type: 'danger' }
}

export function createOrder(payload) {
  return request.post('/orders', payload).then(convertOrder)
}

export function listMyOrders() {
  return request.get('/orders/mine').then((list) => list.map(convertOrder))
}

export function getMyOrder(orderNo) {
  return request.get(`/orders/${orderNo}`).then(convertOrder)
}

export function cancelOrder(orderNo) {
  return request.post(`/orders/${orderNo}/cancel`)
}
