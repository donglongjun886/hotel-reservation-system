<template>
  <div>
    <GuestNav />
    <div class="page" v-if="roomType">
      <el-card class="summary-card">
        <template #header>订单信息</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房型">{{ roomType.name }}</el-descriptions-item>
          <el-descriptions-item label="单价">{{ formatYuan(roomType.price) }} 元/晚</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ checkin }}</el-descriptions-item>
          <el-descriptions-item label="离店日期">{{ checkout }}</el-descriptions-item>
          <el-descriptions-item label="晚数">{{ nights }} 晚</el-descriptions-item>
          <el-descriptions-item label="支付方式">到店支付</el-descriptions-item>
          <el-descriptions-item label="合计金额" :span="2">
            <span class="amount">{{ formatYuan(totalFen) }} 元</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="guest-card">
        <template #header>住客信息</template>
        <el-form label-width="80px">
          <el-form-item label="住客姓名" :error="errors.guestName">
            <el-input v-model="form.guestName" placeholder="请输入住客姓名" />
          </el-form-item>
          <el-form-item label="手机号" :error="errors.guestPhone">
            <el-input v-model="form.guestPhone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>
      </el-card>

      <div class="submit-bar">
        <el-button @click="router.back()">返回修改</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GuestNav from './GuestNav.vue'
import { getRoomType } from '../../api/room'
import { createOrder } from '../../api/order'
import { session } from '../../stores/session'
import { formatYuan } from '../../utils/money'
import { daysBetween } from '../../utils/date'

const route = useRoute()
const router = useRouter()
const checkin = route.query.checkin
const checkout = route.query.checkout
const roomType = ref(null)
const form = reactive({ guestName: '', guestPhone: session.user?.loginName || '' })
const errors = reactive({ guestName: '', guestPhone: '' })
const submitting = ref(false)

onMounted(async () => {
  if (!route.query.roomTypeId || !checkin || !checkout) {
    router.replace('/')
    return
  }
  roomType.value = await getRoomType(route.query.roomTypeId)
})

const nights = computed(() => daysBetween(checkin, checkout))
// 单价为"分"整数，整数乘法无精度问题；仅在模板渲染处格式化
const totalFen = computed(() => (roomType.value ? roomType.value.price * nights.value : 0))
// 幂等请求号：进入本页生成一次，双击/网络重试提交同一 requestNo，服务端据此去重
const requestNo = crypto.randomUUID()

async function submit() {
  errors.guestName = form.guestName && form.guestName.trim() ? '' : '请输入住客姓名'
  errors.guestPhone = /^1[3-9]\d{9}$/.test(form.guestPhone) ? '' : '手机号格式不正确'
  if (errors.guestName || errors.guestPhone) return
  submitting.value = true
  try {
    const order = await createOrder({
      requestNo,
      roomTypeId: Number(route.query.roomTypeId),
      checkinDate: checkin,
      checkoutDate: checkout,
      guestName: form.guestName.trim(),
      guestPhone: form.guestPhone
    })
    router.replace({ path: '/booking/result', query: { status: 'success', orderNo: order.orderNo } })
  } catch (e) {
    const reason = (e && e.message) || '预订失败，请稍后重试'
    router.replace({ path: '/booking/result', query: { status: 'fail', reason } })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  max-width: 720px;
  margin: 0 auto;
  padding: 16px 24px;
}

.summary-card {
  margin-bottom: 16px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.submit-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}
</style>
