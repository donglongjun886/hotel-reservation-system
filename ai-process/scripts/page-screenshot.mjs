// CDP 截图脚本：驱动本机 Chrome headless 截取酒店系统 13 个页面（零依赖，Node ≥ 22 内置 WebSocket/fetch）
// 前提：应用在 localhost:8080 运行，且已存在演示数据（住客 13800138000/Test1234 及订单 HR20261005-0005/0007）
// 用法: node ai-process/scripts/page-screenshot.mjs <输出目录>   例: node ai-process/scripts/page-screenshot.mjs docs/screenshots
import { spawn } from 'node:child_process'
import { writeFileSync, mkdirSync } from 'node:fs'

const CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
const APP = 'http://localhost:8080'
const PORT = 9333
const OUT = process.argv[2] || '/tmp/shots'
mkdirSync(OUT, { recursive: true })

const d = (offset) => {
  const t = new Date(Date.now() + offset * 86400000)
  return `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`
}

async function login(loginName, password) {
  const r = await fetch(`${APP}/api/auth/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ loginName, password })
  }).then(r => r.json())
  if (r.code !== 0) throw new Error(`登录失败: ${JSON.stringify(r)}`)
  return r.data
}

const guest = await login('13800138000', 'Test1234')
const admin = await login('admin', 'admin123')
// 登录响应为扁平结构 {token, role, loginName}，session 存储结构为 {token, user:{role, loginName}}
const sess = (res) => JSON.stringify({ token: res.token, user: { role: res.role, loginName: res.loginName } })

const SHOTS = [
  // 无登录态
  { name: '01-guest-room-list', url: '/', session: null },
  { name: '02-guest-room-detail', url: '/room/1', session: null },
  { name: '03-guest-login', url: '/login', session: null },
  { name: '04-guest-register', url: '/register', session: null },
  { name: '05-admin-login', url: '/admin/login', session: null },
  // 住客登录态
  { name: '06-guest-booking-confirm', url: `/booking/confirm?roomTypeId=1&checkin=${d(1)}&checkout=${d(2)}`, session: sess(guest) },
  { name: '07-guest-booking-result', url: '/booking/result?status=success&orderNo=HR20261005-0005', session: sess(guest) },
  { name: '08-guest-my-orders', url: '/orders', session: sess(guest) },
  { name: '09-guest-order-detail', url: '/orders/HR20261005-0007', session: sess(guest) },
  // admin 登录态
  { name: '10-admin-orders', url: '/admin/orders', session: sess(admin) },
  { name: '11-admin-order-detail', url: '/admin/orders/HR20261005-0005', session: sess(admin) },
  { name: '12-admin-room-types', url: '/admin/room-types', session: sess(admin) },
  { name: '13-admin-rooms', url: '/admin/rooms', session: sess(admin) },
]

const chrome = spawn(CHROME, [
  '--headless=new', `--remote-debugging-port=${PORT}`,
  '--user-data-dir=/tmp/chrome-shot-profile', '--window-size=1440,900',
  '--hide-scrollbars', '--force-device-scale-factor=1', '--no-first-run',
  'about:blank'
], { stdio: 'ignore' })

const sleep = (ms) => new Promise(r => setTimeout(r, ms))

async function getWsUrl() {
  for (let i = 0; i < 50; i++) {
    try {
      const list = await fetch(`http://127.0.0.1:${PORT}/json/list`).then(r => r.json())
      const page = list.find(t => t.type === 'page')
      if (page) return page.webSocketDebuggerUrl
    } catch {}
    await sleep(200)
  }
  throw new Error('Chrome 调试端口未就绪')
}

const ws = new WebSocket(await getWsUrl())
await new Promise((res, rej) => { ws.onopen = res; ws.onerror = rej })

let msgId = 0
const pending = new Map()
let loadResolve = null
ws.onmessage = (ev) => {
  const m = JSON.parse(ev.data)
  if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id) }
  if (m.method === 'Page.loadEventFired' && loadResolve) { loadResolve(); loadResolve = null }
}
function send(method, params = {}) {
  return new Promise((resolve) => {
    const id = ++msgId
    pending.set(id, resolve)
    ws.send(JSON.stringify({ id, method, params }))
  })
}

await send('Page.enable')
await send('Runtime.enable')
await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 900, deviceScaleFactor: 1, mobile: false })

async function navigate(url) {
  const loaded = new Promise(r => { loadResolve = r })
  await send('Page.navigate', { url })
  await Promise.race([loaded, sleep(8000)])
}

for (const shot of SHOTS) {
  // 先到首页建立源，设置/清理登录态，立即刷新让 SPA 以目标登录态重新初始化，
  // 再跳转目标路由（此时路由守卫读到的已是新会话）
  await navigate(`${APP}/`)
  await send('Runtime.evaluate', { expression: shot.session
    ? `localStorage.setItem('hotel_session', ${JSON.stringify(shot.session)})`
    : `localStorage.removeItem('hotel_session')` })
  const reloaded = new Promise(r => { loadResolve = r })
  await send('Page.reload', { ignoreCache: true })
  await Promise.race([reloaded, sleep(8000)])
  await navigate(`${APP}/#${shot.url}`)
  await sleep(1800) // 等 Element Plus 渲染 + 接口数据返回
  const res = await send('Page.captureScreenshot', { format: 'png' })
  if (!res.result?.data) { console.error(`✗ ${shot.name} 截图失败`); continue }
  writeFileSync(`${OUT}/${shot.name}.png`, Buffer.from(res.result.data, 'base64'))
  console.log(`✓ ${shot.name}.png`)
}

ws.close()
chrome.kill()
console.log(`完成，输出目录: ${OUT}`)
process.exit(0)
