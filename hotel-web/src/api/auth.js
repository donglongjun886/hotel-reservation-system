import request from './request'

export function login(loginName, password, config) {
  return request.post('/auth/login', { loginName, password }, config)
}

export function register(phone, password) {
  return request.post('/auth/register', { phone, password })
}

export function logout() {
  return request.post('/auth/logout')
}
