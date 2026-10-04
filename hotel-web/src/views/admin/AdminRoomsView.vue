<template>
  <AdminNav>
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">新增房间</el-button>
      <el-select v-model="filterTypeId" placeholder="全部房型" clearable class="filter" @change="load">
        <el-option v-for="t in roomTypes" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
    </div>

    <el-table v-if="loaded && rooms.length > 0" :data="rooms" border>
      <el-table-column prop="roomNo" label="房间号" width="140" />
      <el-table-column prop="roomTypeName" label="所属房型" min-width="140" />
      <el-table-column label="当前状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.occupied ? 'danger' : 'success'">{{ row.occupied ? '入住中' : '空闲' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else-if="loaded" description="暂无房间，请点击新增" />

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑房间' : '新增房间'" width="420px">
      <el-form label-position="top">
        <el-form-item label="房间号" :error="errors.roomNo">
          <el-input v-model="form.roomNo" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="所属房型" :error="errors.roomTypeId">
          <el-select v-model="form.roomTypeId" placeholder="请选择房型" class="type-select">
            <el-option v-for="t in roomTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </AdminNav>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AdminNav from './AdminNav.vue'
import { listAdminRoomTypes, listAdminRooms, createRoom, updateRoom } from '../../api/admin'

const roomTypes = ref([])
const rooms = ref([])
const loaded = ref(false)
const filterTypeId = ref(null)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = reactive({ roomNo: '', roomTypeId: null })
const errors = reactive({ roomNo: '', roomTypeId: '' })

onMounted(async () => {
  roomTypes.value = await listAdminRoomTypes()
  await load()
})

async function load() {
  rooms.value = await listAdminRooms(filterTypeId.value)
  loaded.value = true
}

function openDialog(room) {
  editing.value = room || null
  form.roomNo = room?.roomNo || ''
  form.roomTypeId = room?.roomTypeId || null
  errors.roomNo = ''
  errors.roomTypeId = ''
  dialogVisible.value = true
}

// TC-F06：房间号为空 → 表单内提示"房间号不能为空且不可重复"（重复由后端同文案拒绝）
function validate() {
  errors.roomNo = form.roomNo.trim() ? '' : '房间号不能为空且不可重复'
  errors.roomTypeId = form.roomTypeId ? '' : '请选择房型'
  return !errors.roomNo && !errors.roomTypeId
}

async function save() {
  if (!validate()) return
  saving.value = true
  try {
    const payload = { roomNo: form.roomNo.trim(), roomTypeId: form.roomTypeId }
    if (editing.value) {
      await updateRoom(editing.value.id, payload)
    } else {
      await createRoom(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.filter {
  width: 200px;
}

.type-select {
  width: 100%;
}
</style>
