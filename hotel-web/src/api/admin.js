import request from './request'

// 前台管理端接口（/api/admin/**，仅 ADMIN 角色）；金额口径同 order.js：接口字段一律"分"整数

// 订单查询（关键字可为订单号或手机号，空查全部；分页）：返回 { list, total, page, pageSize }
export function listOrders(keyword, page = 1, pageSize = 10) {
  return request.get('/admin/orders', { params: { keyword: keyword || undefined, page, pageSize } })
}

export function getOrder(orderNo) {
  return request.get(`/admin/orders/${orderNo}`)
}

// 入住预检（原型 P-A3）：返回 { pass, reasons }，reasons 为不可办理原因文案列表
export function checkInPrecheck(orderNo) {
  return request.get(`/admin/orders/${orderNo}/checkin-precheck`)
}

// 可分配房间下拉（仅该房型当前空闲房间，天然不含在住房间）：返回 [{ id, roomNo }]
export function listAssignableRooms(orderNo) {
  return request.get(`/admin/orders/${orderNo}/assignable-rooms`)
}

export function checkIn(orderNo, idCard, roomNo) {
  return request.post(`/admin/orders/${orderNo}/check-in`, { idCard, roomNo })
}

export function checkOut(orderNo) {
  return request.post(`/admin/orders/${orderNo}/check-out`)
}

// 房型维护：返回 [{ id, name, price, description, roomCount }]
export function listAdminRoomTypes() {
  return request.get('/admin/room-types')
}

export function createRoomType(payload) {
  return request.post('/admin/room-types', payload)
}

export function updateRoomType(id, payload) {
  return request.put(`/admin/room-types/${id}`, payload)
}

// 房间维护：返回 [{ id, roomNo, roomTypeId, roomTypeName, occupied }]
export function listAdminRooms(roomTypeId) {
  return request.get('/admin/rooms', { params: { roomTypeId: roomTypeId || undefined } })
}

export function createRoom(payload) {
  return request.post('/admin/rooms', payload)
}

export function updateRoom(id, payload) {
  return request.put(`/admin/rooms/${id}`, payload)
}
