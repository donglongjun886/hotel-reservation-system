<template>
  <div>
    <GuestNav />
    <div class="page">
      <template v-if="loaded && orders.length === 0">
        <el-empty description="您还没有订单，快去预订吧">
          <el-button type="primary" @click="router.push('/')">去预订</el-button>
        </el-empty>
      </template>

      <el-card v-for="order in orders" :key="order.orderNo" class="order-card" shadow="hover">
        <div class="card-body">
          <div class="info">
            <div class="title-row">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <el-tag :type="statusOf(order).type">{{ statusOf(order).text }}</el-tag>
            </div>
            <div class="line">房型：{{ order.roomTypeName }}</div>
            <div class="line">入住/离店：{{ order.checkinDate }} ~ {{ order.checkoutDate }}</div>
            <div class="line amount">金额：{{ formatYuan(order.amount) }} 元</div>
          </div>
          <div class="actions">
            <el-button @click="router.push(`/orders/${order.orderNo}`)">查看详情</el-button>
          </div>
        </div>
      </el-card>

      <el-pagination
        v-if="total > pageSize"
        class="pager"
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import GuestNav from './GuestNav.vue'
import { listMyOrders, ORDER_STATUS } from '../../api/order'
import { formatYuan } from '../../utils/money'

const router = useRouter()
const orders = ref([])
const loaded = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
let requestSeq = 0

onMounted(() => load(1))

async function load(p) {
  const seq = ++requestSeq
  const result = await listMyOrders(p, pageSize)
  if (seq !== requestSeq) return
  orders.value = result.list
  total.value = result.total
  page.value = result.page
  loaded.value = true
}

function statusOf(order) {
  return ORDER_STATUS[order.status] || { text: order.status, type: 'info' }
}
</script>

<style scoped>
.page {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px 24px;
}

.order-card {
  margin-bottom: 12px;
}

.card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.order-no {
  font-weight: 600;
}

.line {
  color: #606266;
  margin: 2px 0;
}

.amount {
  color: #f56c6c;
}

.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
