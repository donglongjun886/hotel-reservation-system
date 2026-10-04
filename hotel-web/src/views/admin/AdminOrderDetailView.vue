<template>
  <AdminNav>
    <div class="page" v-if="order">
      <div class="status-row">
        <el-tag :type="statusOf.type" size="large">{{ statusOf.text }}</el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号" :span="2">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ order.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="晚数">{{ order.nights }} 晚</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ order.checkinDate }}</el-descriptions-item>
        <el-descriptions-item label="离店日期">{{ order.checkoutDate }}</el-descriptions-item>
        <el-descriptions-item label="住客姓名">{{ order.guestName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ order.guestPhone }}</el-descriptions-item>
        <el-descriptions-item label="金额">
          <span class="amount">{{ formatYuan(order.amount) }} 元</span>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ createdAt }}</el-descriptions-item>
        <el-descriptions-item v-if="order.roomNo" label="房间号">{{ order.roomNo }}</el-descriptions-item>
        <el-descriptions-item v-if="order.idCard" label="身份证号">{{ order.idCard }}</el-descriptions-item>
      </el-descriptions>

      <!-- 办理入住操作区：仅状态=已确认时出现（TC-D06①） -->
      <el-card v-if="order.status === 'CONFIRMED'" class="op-card" shadow="never">
        <template #header>办理入住</template>
        <el-alert
          v-if="precheck"
          :type="precheck.pass ? 'success' : 'error'"
          :closable="false"
          class="precheck"
        >
          <template v-if="precheck.pass">入住条件校验通过，可办理入住</template>
          <template v-else>
            <div>入住条件校验不通过：</div>
            <div v-for="reason in precheck.reasons" :key="reason">{{ reason }}</div>
          </template>
        </el-alert>
        <el-form label-position="top">
          <el-form-item label="身份证号" :error="errors.idCard">
            <el-input v-model="checkInForm.idCard" placeholder="请输入18位身份证号" />
          </el-form-item>
          <el-form-item label="分配房间" :error="errors.roomNo">
            <el-select v-model="checkInForm.roomNo" placeholder="请选择房间" class="room-select">
              <el-option v-for="room in assignableRooms" :key="room.id" :label="room.roomNo" :value="room.roomNo" />
            </el-select>
          </el-form-item>
          <el-button type="primary" :loading="checkingIn" @click="onCheckIn">确认入住</el-button>
        </el-form>
      </el-card>

      <!-- 办理退房操作区：仅状态=已入住时出现 -->
      <el-card v-if="order.status === 'CHECKED_IN'" class="op-card" shadow="never">
        <template #header>办理退房</template>
        <el-button type="danger" :loading="checkingOut" @click="onCheckOut">确认退房</el-button>
      </el-card>

      <div class="actions">
        <el-button @click="router.push('/admin/orders')">返回列表</el-button>
      </div>
    </div>
  </AdminNav>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminNav from './AdminNav.vue'
import { getOrder, checkInPrecheck, listAssignableRooms, checkIn, checkOut } from '../../api/admin'
import { ORDER_STATUS } from '../../api/order'
import { formatYuan } from '../../utils/money'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const precheck = ref(null)
const assignableRooms = ref([])
const checkInForm = reactive({ idCard: '', roomNo: '' })
const errors = reactive({ idCard: '', roomNo: '' })
const checkingIn = ref(false)
const checkingOut = ref(false)

const ID_CARD_PATTERN = /^\d{17}[\dXx]$/

onMounted(load)

async function load() {
  const orderNo = route.params.orderNo
  order.value = await getOrder(orderNo)
  if (order.value.status === 'CONFIRMED') {
    ;[precheck.value, assignableRooms.value] = await Promise.all([
      checkInPrecheck(orderNo),
      listAssignableRooms(orderNo)
    ])
  }
}

const statusOf = computed(() => ORDER_STATUS[order.value?.status] || { text: order.value?.status, type: 'info' })
const createdAt = computed(() => (order.value?.createdAt || '').replace('T', ' '))

async function onCheckIn() {
  errors.idCard = ID_CARD_PATTERN.test(checkInForm.idCard.trim()) ? '' : '身份证号格式不正确'
  errors.roomNo = checkInForm.roomNo ? '' : '请选择房间'
  if (errors.idCard || errors.roomNo) return
  checkingIn.value = true
  try {
    await checkIn(order.value.orderNo, checkInForm.idCard.trim(), checkInForm.roomNo)
    ElMessage.success('入住办理成功')
    checkInForm.idCard = ''
    checkInForm.roomNo = ''
    precheck.value = null
    assignableRooms.value = []
    await load()
  } finally {
    checkingIn.value = false
  }
}

async function onCheckOut() {
  try {
    await ElMessageBox.confirm('确认退房？', '办理退房', {
      confirmButtonText: '确认退房',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  checkingOut.value = true
  try {
    await checkOut(order.value.orderNo)
    ElMessage.success('退房办理成功')
    router.push('/admin/orders')
  } finally {
    checkingOut.value = false
  }
}
</script>

<style scoped>
.page {
  max-width: 720px;
  margin: 0 auto;
}

.status-row {
  margin-bottom: 16px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.op-card {
  margin-top: 16px;
}

.precheck {
  margin-bottom: 16px;
}

.room-select {
  width: 240px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}
</style>
