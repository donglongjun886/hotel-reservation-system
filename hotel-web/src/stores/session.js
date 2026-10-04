import { reactive } from 'vue'

const STORAGE_KEY = 'hotel_session'

function load() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {}
  } catch {
    return {}
  }
}

const saved = load()

export const session = reactive({
  token: saved.token || '',
  user: saved.user || null
})

export function setSession(token, user) {
  session.token = token
  session.user = user
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ token, user }))
}

export function clearSession() {
  session.token = ''
  session.user = null
  localStorage.removeItem(STORAGE_KEY)
}
