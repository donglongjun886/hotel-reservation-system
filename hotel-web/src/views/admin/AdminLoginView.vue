<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">酒店管理后台</h2>
      <el-form label-position="top">
        <el-form-item label="账号" :error="errors.loginName">
          <el-input v-model="form.loginName" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" :error="errors.password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { setSession } from '../../stores/session'

const router = useRouter()
const form = reactive({ loginName: '', password: '' })
const errors = reactive({ loginName: '', password: '' })
const loading = ref(false)

async function submit() {
  errors.loginName = form.loginName ? '' : '请输入账号'
  errors.password = form.password ? '' : '请输入密码'
  if (errors.loginName || errors.password) return
  loading.value = true
  try {
    const res = await login(form.loginName, form.password, { skipErrorToast: true })
    if (res.role !== 'ADMIN') {
      ElMessage.error('账号或密码错误')
      return
    }
    setSession(res.token, { role: res.role, loginName: res.loginName })
    router.push('/admin/orders')
  } catch (e) {
    // 业务错误（登录失败）才在此提示；网络/HTTP 错误 request.js 已弹"网络异常，请稍后重试"
    if (typeof e?.code === 'number') {
      ElMessage.error('账号或密码错误')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  padding-top: 80px;
}

.login-card {
  width: 380px;
}

.title {
  text-align: center;
  margin-top: 0;
}

.submit {
  width: 100%;
}
</style>
