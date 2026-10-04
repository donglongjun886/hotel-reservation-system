import request from './request'

// 金额口径：price 为"分"整数，组件持分、仅在模板渲染处用 formatYuan 格式化（utils/money.js）
export function listRoomTypes() {
  return request.get('/room-types')
}

export function getRoomType(id) {
  return request.get(`/room-types/${id}`)
}

export function queryAvailability(checkin, checkout) {
  return request.get('/room-types/availability', { params: { checkin, checkout } })
}
