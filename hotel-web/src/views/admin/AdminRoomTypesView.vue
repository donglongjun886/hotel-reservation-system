<template>
  <AdminNav>
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">新增房型</el-button>
    </div>

    <el-table v-if="loaded && roomTypes.length > 0" :data="roomTypes" border>
      <el-table-column prop="name" label="房型名称" min-width="140" />
      <el-table-column label="单价" width="120">
        <template #default="{ row }">{{ formatYuan(row.price) }} 元/晚</template>
      </el-table-column>
      <el-table-column prop="description" label="介绍" min-width="200" show-overflow-tooltip />
      <el-table-column prop="roomCount" label="房间数" width="90" />
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else-if="loaded" description="暂无房型，请点击新增" />

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑房型' : '新增房型'" width="420px">
      <el-form label-position="top">
        <el-form-item label="房型名称" :error="errors.name">
          <el-input v-model="form.name" placeholder="请输入房型名称" />
        </el-form-item>
        <el-form-item label="单价（元/晚）" :error="errors.price">
          <el-input-number v-model="form.priceYuan" :min="0" :precision="2" :controls="false" class="price-input" />
        </el-form-item>
        <el-form-item label="介绍">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入房型介绍" />
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
import { listAdminRoomTypes, createRoomType, updateRoomType } from '../../api/admin'
import { formatYuan, yuanToFen, fenToYuan } from '../../utils/money'

const roomTypes = ref([])
const loaded = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = reactive({ name: '', priceYuan: undefined, description: '' })
const errors = reactive({ name: '', price: '' })

onMounted(load)

async function load() {
  roomTypes.value = await listAdminRoomTypes()
  loaded.value = true
}

function openDialog(roomType) {
  editing.value = roomType || null
  form.name = roomType?.name || ''
  form.priceYuan = roomType ? fenToYuan(roomType.price) : undefined
  form.description = roomType?.description || ''
  errors.name = ''
  errors.price = ''
  dialogVisible.value = true
}

// TC-F05：名称/单价为空或单价非正数 → 表单内提示"请填写完整的房型信息"
function validate() {
  errors.name = form.name.trim() ? '' : '请填写完整的房型信息'
  errors.price = form.priceYuan > 0 ? '' : '请填写完整的房型信息'
  return !errors.name && !errors.price
}

async function save() {
  if (!validate()) return
  saving.value = true
  try {
    const payload = { name: form.name.trim(), price: yuanToFen(form.priceYuan), description: form.description.trim() }
    if (editing.value) {
      await updateRoomType(editing.value.id, payload)
    } else {
      await createRoomType(payload)
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
  margin-bottom: 16px;
}

.price-input {
  width: 200px;
}
</style>
