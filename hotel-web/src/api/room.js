import request from './request'

// 金额口径：接口传"分"整数，分→元换算只允许在 api 层，组件拿到的就是元
function toYuan(fen) {
  return fen / 100
}

function convertRoomType(info) {
  return { ...info, price: toYuan(info.price) }
}

export function listRoomTypes() {
  return request.get('/room-types').then((list) => list.map(convertRoomType))
}

export function getRoomType(id) {
  return request.get(`/room-types/${id}`).then(convertRoomType)
}

export function queryAvailability(checkin, checkout) {
  return request.get('/room-types/availability', { params: { checkin, checkout } })
}
