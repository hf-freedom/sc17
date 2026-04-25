<template>
  <div class="schedule-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排班管理</span>
          <div class="header-actions">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              @change="fetchSchedules"
            />
            <el-button type="primary" @click="openAddDialog">添加排班</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="schedules" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeId" label="员工ID" width="80" />
        <el-table-column label="员工姓名" width="100">
          <template #default="{ row }">
            {{ getEmployeeName(row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="shiftId" label="班次ID" width="80" />
        <el-table-column label="班次信息" width="300">
          <template #default="{ row }">
            {{ getShiftInfo(row.shiftId) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="deleteSchedule(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="添加排班" width="500px">
      <el-form :model="newSchedule" label-width="100px">
        <el-form-item label="员工">
          <el-select v-model="newSchedule.employeeId" placeholder="请选择员工" style="width: 100%">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="newSchedule.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="班次">
          <el-select v-model="newSchedule.shiftId" placeholder="请选择班次" style="width: 100%">
            <el-option
              v-for="shift in shifts"
              :key="shift.id"
              :label="`${shift.name} (${shift.startTime} - ${shift.endTime})`"
              :value="shift.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const selectedDate = ref(new Date())
const schedules = ref([])
const employees = ref([])
const shifts = ref([])
const dialogVisible = ref(false)
const newSchedule = ref({
  employeeId: null,
  date: '',
  shiftId: null
})

const getEmployeeName = (employeeId) => {
  const emp = employees.value.find(e => e.id === employeeId)
  return emp ? emp.name : '未知'
}

const getShiftInfo = (shiftId) => {
  const shift = shifts.value.find(s => s.id === shiftId)
  return shift ? `${shift.name} (${shift.startTime} - ${shift.endTime})` : '未知'
}

const fetchEmployees = async () => {
  try {
    const res = await axios.get('/api/employees')
    employees.value = res.data
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  }
}

const fetchShifts = async () => {
  try {
    const res = await axios.get('/api/shifts')
    shifts.value = res.data
  } catch (error) {
    ElMessage.error('获取班次列表失败')
  }
}

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const fetchSchedules = async () => {
  try {
    const dateStr = formatDate(selectedDate.value)
    const res = await axios.get(`/api/schedules/date/${dateStr}`)
    schedules.value = res.data
  } catch (error) {
    ElMessage.error('获取排班列表失败')
  }
}

const openAddDialog = () => {
  newSchedule.value = {
    employeeId: null,
    date: formatDate(selectedDate.value),
    shiftId: null
  }
  dialogVisible.value = true
}

const addSchedule = async () => {
  if (!newSchedule.value.employeeId || !newSchedule.value.date || !newSchedule.value.shiftId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await axios.post('/api/schedules', newSchedule.value)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchSchedules()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const deleteSchedule = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`/api/schedules/${id}`)
    ElMessage.success('删除成功')
    fetchSchedules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchEmployees()
  fetchShifts()
  fetchSchedules()
})
</script>

<style scoped>
.schedule-page {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
