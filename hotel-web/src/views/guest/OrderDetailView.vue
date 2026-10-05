<template>
  <div>
    <GuestNav />
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
        <el-descriptions-item v-if="order.roomNo" label="房间号" :span="2">{{ order.roomNo }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button @click="router.push('/orders')">返回列表</el-button>
        <el-button v-if="order.status === 'CONFIRMED'" type="danger" :loading="cancelling" @click="onCancel">
          取消订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import GuestNav from './GuestNav.vue'
import { getMyOrder, cancelOrder, ORDER_STATUS } from '../../api/order'
import { formatYuan } from '../../utils/money'
import { formatDateTime } from '../../utils/date'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const cancelling = ref(false)

onMounted(load)

async function load() {
  order.value = await getMyOrder(route.params.orderNo)
}

const statusOf = computed(() => ORDER_STATUS[order.value?.status] || { text: order.value?.status, type: 'info' })
const createdAt = computed(() => formatDateTime(order.value?.createdAt))

async function onCancel() {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
  } catch {
    return
  }
  cancelling.value = true
  try {
    await cancelOrder(order.value.orderNo)
    ElMessage.success('取消成功')
    await load()
  } finally {
    cancelling.value = false
  }
}
</script>

<style scoped>
.page {
  max-width: 720px;
  margin: 0 auto;
  padding: 16px 24px;
}

.status-row {
  margin-bottom: 16px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}
</style>
