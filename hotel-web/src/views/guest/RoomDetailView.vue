<template>
  <div>
    <GuestNav />
    <div class="page" v-if="roomType">
      <div class="img-placeholder">房型图片</div>
      <h2>{{ roomType.name }}</h2>
      <div class="price">{{ formatYuan(roomType.price) }} 元/晚</div>
      <p class="desc">{{ roomType.description }}</p>

      <el-divider />
      <div class="date-info">
        <span>入住：{{ checkin }}</span>
        <span>离店：{{ checkout }}</span>
        <span>晚数：{{ nights }} 晚</span>
        <span class="subtotal">小计：{{ formatYuan(subtotalFen) }} 元</span>
        <el-button size="small" @click="editing = !editing">修改日期</el-button>
      </div>
      <div v-if="editing" class="date-edit">
        <el-date-picker v-model="checkin" type="date" placeholder="入住日期" value-format="YYYY-MM-DD" :clearable="false" @change="onDateChange" />
        <el-date-picker v-model="checkout" type="date" placeholder="离店日期" value-format="YYYY-MM-DD" :clearable="false" @change="onDateChange" />
      </div>

      <div class="bottom-bar">
        <el-button type="primary" size="large" :disabled="nights <= 0" @click="book">立即预订</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import GuestNav from './GuestNav.vue'
import { getRoomType } from '../../api/room'
import { formatYuan } from '../../utils/money'
import { today, tomorrow, daysBetween } from '../../utils/date'

const route = useRoute()
const router = useRouter()
const roomType = ref(null)
const checkin = ref(route.query.checkin || today())
const checkout = ref(route.query.checkout || tomorrow())
const editing = ref(false)

onMounted(async () => {
  roomType.value = await getRoomType(route.params.id)
})

const nights = computed(() => daysBetween(checkin.value, checkout.value))
// 单价为"分"整数，整数乘法无精度问题；仅在模板渲染处格式化
const subtotalFen = computed(() => (roomType.value && nights.value > 0 ? roomType.value.price * nights.value : 0))

function onDateChange() {
  if (!checkin.value || !checkout.value) {
    ElMessage.error('请选择入住和离店日期')
    return
  }
  if (checkout.value <= checkin.value) {
    ElMessage.error('离店日期必须晚于入住日期')
  }
}

function book() {
  if (!checkin.value || !checkout.value) {
    ElMessage.error('请选择入住和离店日期')
    return
  }
  if (checkout.value <= checkin.value) {
    ElMessage.error('离店日期必须晚于入住日期')
    return
  }
  router.push({
    path: '/booking/confirm',
    query: { roomTypeId: roomType.value.id, checkin: checkin.value, checkout: checkout.value }
  })
}
</script>

<style scoped>
.page {
  max-width: 720px;
  margin: 0 auto;
  padding: 16px 24px 80px;
}

.img-placeholder {
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #909399;
  border-radius: 4px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.desc {
  color: #606266;
}

.date-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.subtotal {
  color: #f56c6c;
  font-weight: 600;
}

.date-edit {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 12px 24px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  text-align: center;
}
</style>
