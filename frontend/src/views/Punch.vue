<template>
  <div class="punch-page">
    <el-card>
      <template #header>
        <span>打卡</span>
      </template>
      
      <div class="punch-content">
        <el-form label-width="100px" style="max-width: 400px; margin: 0 auto;">
          <el-form-item label="选择员工">
            <el-select v-model="selectedEmployee" placeholder="请选择员工" style="width: 100%" @change="fetchPunchRecords">
              <el-option
                v-for="emp in employees"
                :key="emp.id"
                :label="emp.name"
                :value="emp.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="当前时间">
            <div class="current-time">{{ currentTime }}</div>
          </el-form-item>
          
          <el-form-item>
            <div class="punch-buttons">
              <el-button 
                type="success" 
                size="large" 
                @click="clockIn"
                :disabled="!selectedEmployee"
              >
                上班打卡
              </el-button>
              <el-button 
                type="warning" 
                size="large" 
                @click="clockOut"
                :disabled="!selectedEmployee"
              >
                下班打卡
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>今日打卡记录</span>
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            @change="fetchPunchRecords"
          />
        </div>
      </template>
      
      <el-table :data="punchRecords" border stripe v-if="selectedEmployee">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="punchTime" label="打卡时间" width="200" />
        <el-table-column prop="punchType" label="打卡类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.punchType === 'CLOCK_IN' ? 'success' : 'warning'">
              {{ row.punchType === 'CLOCK_IN' ? '上班打卡' : '下班打卡' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty description="请先选择员工查看打卡记录" v-else />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const employees = ref([])
const selectedEmployee = ref(null)
const selectedDate = ref(new Date())
const currentTime = ref('')
const punchRecords = ref([])
let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN')
}

const fetchEmployees = async () => {
  try {
    const res = await axios.get('/api/employees')
    employees.value = res.data
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  }
}

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const fetchPunchRecords = async () => {
  if (!selectedEmployee.value) return
  try {
    const dateStr = formatDate(selectedDate.value)
    const res = await axios.get(`/api/punch/employee/${selectedEmployee.value}?date=${dateStr}`)
    punchRecords.value = res.data
  } catch (error) {
    ElMessage.error('获取打卡记录失败')
  }
}

const clockIn = async () => {
  try {
    const res = await axios.post(`/api/punch/in?employeeId=${selectedEmployee.value}`)
    ElMessage.success('上班打卡成功')
    fetchPunchRecords()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '打卡失败')
  }
}

const clockOut = async () => {
  try {
    const res = await axios.post(`/api/punch/out?employeeId=${selectedEmployee.value}`)
    ElMessage.success('下班打卡成功')
    fetchPunchRecords()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '打卡失败')
  }
}

onMounted(() => {
  fetchEmployees()
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.punch-page {
  width: 100%;
}

.punch-content {
  padding: 20px 0;
}

.current-time {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.punch-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
