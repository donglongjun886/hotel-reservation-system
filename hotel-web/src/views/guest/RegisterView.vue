<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2 class="title">注册</h2>
      <el-form label-position="top">
        <el-form-item label="手机号" :error="errors.phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" :error="errors.password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" :error="errors.confirm">
          <el-input v-model="form.confirm" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">注册</el-button>
        <div class="link-row">
          <el-link type="primary" @click="router.push('/login')">去登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/auth'

const router = useRouter()
const form = reactive({ phone: '', password: '', confirm: '' })
const errors = reactive({ phone: '', password: '', confirm: '' })
const loading = ref(false)

async function submit() {
  errors.phone = ''
  errors.password = ''
  errors.confirm = ''
  if (!form.phone) {
    errors.phone = '请输入手机号'
  } else if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    errors.phone = '手机号格式不正确'
  }
  if (!form.password) {
    errors.password = '请输入密码'
  }
  if (form.password && form.confirm !== form.password) {
    errors.confirm = '两次输入的密码不一致'
  }
  if (errors.phone || errors.password || errors.confirm) return
  loading.value = true
  try {
    await register(form.phone, form.password)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.register-card {
  width: 380px;
}

.title {
  text-align: center;
  margin-top: 0;
}

.submit {
  width: 100%;
}

.link-row {
  margin-top: 12px;
  text-align: center;
}
</style>
