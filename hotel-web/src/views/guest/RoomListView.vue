<template>
  <div>
    <GuestNav />
    <div class="page">
      <div class="date-bar">
        <el-date-picker v-model="checkin" type="date" placeholder="入住日期" value-format="YYYY-MM-DD" :clearable="false" />
        <el-date-picker v-model="checkout" type="date" placeholder="离店日期" value-format="YYYY-MM-DD" :clearable="false" />
        <el-button type="primary" :loading="loading" @click="query">查询</el-button>
      </div>

      <el-empty v-if="!loading && roomTypes.length === 0" description="暂无房型，敬请期待" />

      <el-card v-for="type in roomTypes" :key="type.id" class="room-card" shadow="hover">
        <div class="card-body">
          <div class="img-placeholder">房型图片</div>
          <div class="info">
            <div class="title-row">
              <span class="name">{{ type.name }}</span>
              <el-tag :type="remaining(type) > 0 ? 'success' : 'danger'">
                {{ remaining(type) > 0 ? '可订' : '已订满' }}
              </el-tag>
            </div>
            <div class="price">{{ formatYuan(type.price) }} 元/晚</div>
            <div class="desc">{{ type.description }}</div>
            <div class="remain">剩余可订数量：{{ remaining(type) }}</div>
          </div>
          <div class="actions">
            <el-button @click="goDetail(type)">查看详情</el-button>
            <el-button type="primary" :disabled="remaining(type) <= 0" @click="book(type)">立即预订</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import GuestNav from './GuestNav.vue'
import { listRoomTypes, queryAvailability } from '../../api/room'
import { formatYuan } from '../../utils/money'
import { today, tomorrow } from '../../utils/date'

const router = useRouter()
const checkin = ref(today())
const checkout = ref(tomorrow())
const roomTypes = ref([])
const availability = ref({})
const loading = ref(false)

onMounted(query)

async function query() {
  if (!checkin.value || !checkout.value) {
    ElMessage.error('请选择入住和离店日期')
    return
  }
  if (checkout.value <= checkin.value) {
    ElMessage.error('离店日期必须晚于入住日期')
    return
  }
  loading.value = true
  try {
    const [types, avail] = await Promise.all([
      listRoomTypes(),
      queryAvailability(checkin.value, checkout.value)
    ])
    roomTypes.value = types
    availability.value = Object.fromEntries(avail.map((a) => [a.roomTypeId, a.available]))
  } finally {
    loading.value = false
  }
}

function remaining(type) {
  return availability.value[type.id] ?? 0
}

function goDetail(type) {
  router.push({ path: `/room/${type.id}`, query: { checkin: checkin.value, checkout: checkout.value } })
}

function book(type) {
  router.push({
    path: '/booking/confirm',
    query: { roomTypeId: type.id, checkin: checkin.value, checkout: checkout.value }
  })
}
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 16px 24px;
}

.date-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.room-card {
  margin-bottom: 16px;
}

.card-body {
  display: flex;
  gap: 16px;
}

.img-placeholder {
  width: 160px;
  height: 110px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #909399;
  border-radius: 4px;
}

.info {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 17px;
  font-weight: 600;
}

.price {
  color: #f56c6c;
  font-weight: 600;
  margin: 6px 0;
}

.desc {
  color: #606266;
  margin-bottom: 6px;
}

.remain {
  color: #909399;
  font-size: 13px;
}

.actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.actions .el-button {
  margin-left: 0;
}
</style>
