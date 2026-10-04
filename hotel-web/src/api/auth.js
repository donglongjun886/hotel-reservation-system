import request from './request'

export function login(loginName, password) {
  return request.post('/auth/login', { loginName, password })
}

export function register(phone, password) {
  return request.post('/auth/register', { phone, password })
}

export function logout() {
  return request.post('/auth/logout')
}
