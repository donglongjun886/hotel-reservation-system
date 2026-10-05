<template>
  <AdminNav>
    <div class="query-bar">
      <el-input
        v-model="keyword"
        class="keyword"
        placeholder="订单号或手机号"
        clearable
        @keyup.enter="search"
      />
      <el-button type="primary" :loading="loading" @click="search">查询</el-button>
    </div>

    <el-table v-if="loaded && orders.length > 0" :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="guestName" label="住客姓名" width="100" />
      <el-table-column prop="guestPhone" label="手机号" width="130" />
      <el-table-column prop="roomTypeName" label="房型" min-width="120" />
      <el-table-column prop="checkinDate" label="入住日期" width="110" />
      <el-table-column prop="checkoutDate" label="离店日期" width="110" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }">{{ formatYuan(row.amount) }} 元</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusOf(row).type">{{ statusOf(row).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="router.push(`/admin/orders/${row.orderNo}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else-if="loaded" description="暂无订单" />

    <el-pagination
      v-if="total > pageSize"
      class="pager"
      layout="prev, pager, next, total"
      :total="total"
      :page-size="pageSize"
      :current-page="page"
      @current-change="load"
    />
  </AdminNav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminNav from './AdminNav.vue'
import { listOrders } from '../../api/admin'
import { ORDER_STATUS } from '../../api/order'
import { formatYuan } from '../../utils/money'
import { formatDateTime } from '../../utils/date'

const router = useRouter()
const keyword = ref('')
const orders = ref([])
const loaded = ref(false)
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
let requestSeq = 0

onMounted(() => load(1))

// 查询条件为空时默认展示全部订单（原型 P-A2，不报错）
function search() {
  load(1)
}

async function load(p) {
  const seq = ++requestSeq
  loading.value = true
  try {
    const result = await listOrders(keyword.value.trim(), p, pageSize)
    if (seq !== requestSeq) return
    orders.value = result.list
    total.value = result.total
    page.value = result.page
    loaded.value = true
  } finally {
    if (seq === requestSeq) loading.value = false
  }
}

function statusOf(order) {
  return ORDER_STATUS[order.status] || { text: order.status, type: 'info' }
}
</script>

<style scoped>
.query-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.keyword {
  width: 280px;
}

.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
