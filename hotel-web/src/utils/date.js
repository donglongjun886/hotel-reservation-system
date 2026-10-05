export function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function today() {
  return formatDate(new Date())
}

export function tomorrow() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return formatDate(d)
}

export function daysBetween(checkin, checkout) {
  return Math.round((new Date(checkout) - new Date(checkin)) / 86400000)
}

export function formatDateTime(value) {
  return (value || '').replace('T', ' ')
}
