<template>
  <div class="guest-nav">
    <span class="brand" @click="goHome">酒店预订系统</span>
    <div class="nav-right">
      <template v-if="session.token">
        <el-link class="nav-item" @click="router.push('/orders')">我的订单</el-link>
        <span class="phone">{{ session.user?.loginName }}</span>
        <el-link class="nav-item" @click="onLogout">退出</el-link>
      </template>
      <template v-else>
        <el-link class="nav-item" @click="router.push('/login')">登录</el-link>
        <el-link class="nav-item" @click="router.push('/register')">注册</el-link>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { session, clearSession } from '../../stores/session'
import { logout } from '../../api/auth'

const router = useRouter()

function goHome() {
  router.push('/')
}

function onLogout() {
  logout().catch(() => {})
  clearSession()
  router.push('/')
}
</script>

<style scoped>
.guest-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.brand {
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.phone {
  color: #606266;
}
</style>
