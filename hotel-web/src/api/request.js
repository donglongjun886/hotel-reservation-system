import axios from 'axios'
import { ElMessage } from 'element-plus'
import { session, clearSession } from '../stores/session'
import router from '../router'

const request = axios.create({ baseURL: '/api', timeout: 10000 })

request.interceptors.request.use((config) => {
  if (session.token) {
    config.headers['X-Token'] = session.token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code !== 0) {
      if (!response.config.skipErrorToast) {
        ElMessage.error(body.message || '操作失败')
      }
      return Promise.reject(body)
    }
    return body.data
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      clearSession()
      const isAdmin = router.currentRoute.value.path.startsWith('/admin')
      ElMessage.error('登录已失效，请重新登录')
      router.push(isAdmin ? '/admin/login' : '/login')
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
