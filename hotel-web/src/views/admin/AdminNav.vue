<template>
  <div class="admin-layout">
    <aside class="side">
      <div class="brand">酒店管理后台</div>
      <el-menu :default-active="active" @select="onSelect">
        <el-menu-item index="/admin/orders">订单查询</el-menu-item>
        <el-menu-item index="/admin/room-types">房型管理</el-menu-item>
        <el-menu-item index="/admin/rooms">房间管理</el-menu-item>
      </el-menu>
    </aside>
    <div class="main">
      <div class="topbar">
        <span class="who">{{ session.user?.loginName }}</span>
        <el-link class="logout" @click="onLogout">退出</el-link>
      </div>
      <div class="content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { session, clearSession } from '../../stores/session'
import { logout } from '../../api/auth'

const route = useRoute()
const router = useRouter()

// 订单详情页归属"订单查询"菜单
const active = computed(() => (route.path.startsWith('/admin/orders') ? '/admin/orders' : route.path))

function onSelect(path) {
  if (path !== route.path) router.push(path)
}

function onLogout() {
  logout().catch(() => {})
  clearSession()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.side {
  width: 180px;
  border-right: 1px solid #e4e7ed;
  background: #fff;
}

.brand {
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #e4e7ed;
}

.side :deep(.el-menu) {
  border-right: none;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  padding: 12px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.who {
  color: #606266;
}

.content {
  padding: 16px 24px;
}
</style>
