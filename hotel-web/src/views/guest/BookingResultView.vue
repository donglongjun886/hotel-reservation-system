<template>
  <div class="result-page">
    <el-card class="result-card">
      <template v-if="isSuccess">
        <div class="icon success">✓</div>
        <h2>预订成功</h2>
        <div class="order-no">{{ route.query.orderNo }}</div>
        <p class="tip">入住时请出示订单号</p>
        <el-descriptions v-if="order" :column="1" border class="summary">
          <el-descriptions-item label="房型">{{ order.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="入住/离店">{{ order.checkinDate }} ~ {{ order.checkoutDate }}</el-descriptions-item>
          <el-descriptions-item label="金额">{{ order.amount }} 元</el-descriptions-item>
        </el-descriptions>
        <div class="actions">
          <el-button type="primary" @click="router.push('/orders')">查看我的订单</el-button>
          <el-button @click="router.push('/')">返回首页</el-button>
        </div>
      </template>
      <template v-else>
        <div class="icon fail">✕</div>
        <h2>预订失败</h2>
        <p class="reason">{{ route.query.reason }}</p>
        <div class="actions">
          <el-button type="primary" @click="router.push('/')">重新选择房型</el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMyOrder } from '../../api/order'

const route = useRoute()
const router = useRouter()
const order = ref(null)

const isSuccess = computed(() => route.query.status === 'success')

onMounted(async () => {
  if (isSuccess.value && route.query.orderNo) {
    order.value = await getMyOrder(route.query.orderNo)
  }
})
</script>

<style scoped>
.result-page {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.result-card {
  width: 460px;
  text-align: center;
}

.icon {
  width: 56px;
  height: 56px;
  margin: 0 auto;
  border-radius: 50%;
  font-size: 32px;
  line-height: 56px;
  color: #fff;
}

.icon.success {
  background: #67c23a;
}

.icon.fail {
  background: #f56c6c;
}

.order-no {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
  margin: 8px 0;
}

.tip {
  color: #909399;
}

.reason {
  color: #f56c6c;
}

.summary {
  margin: 16px 0;
  text-align: left;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}
</style>
