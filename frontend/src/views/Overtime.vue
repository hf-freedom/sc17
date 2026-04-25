<template>
  <div class="overtime-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>加班申请</span>
          <el-button type="primary" @click="openAddDialog">新增申请</el-button>
        </div>
      </template>
      
      <el-table :data="overtimeRequests" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeId" label="员工ID" width="80" />
        <el-table-column label="员工姓名" width="100">
          <template #default="{ row }">
            {{ getEmployeeName(row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="hours" label="加班时长" width="100">
          <template #default="{ row }">
            {{ row.hours }} 小时
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="加班原因" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="success" 
              size="small" 
              @click="approveOvertime(row.id)"
            >
              审批通过
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="danger" 
              size="small" 
              @click="rejectOvertime(row.id)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="申请加班" width="500px">
      <el-form :model="newOvertime" label-width="100px">
        <el-form-item label="员工">
          <el-select v-model="newOvertime.employeeId" placeholder="请选择员工" style="width: 100%">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="newOvertime.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="newOvertime.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="加班原因">
          <el-input v-model="newOvertime.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOvertime">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const employees = ref([])
const overtimeRequests = ref([])
const dialogVisible = ref(false)
const newOvertime = ref({
  employeeId: null,
  startTime: '',
  endTime: '',
  reason: ''
})

const getEmployeeName = (employeeId) => {
  const emp = employees.value.find(e => e.id === employeeId)
  return emp ? emp.name : '未知'
}

const getStatusText = (status) => {
  const statuses = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return statuses[status] || status
}

const getStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || 'info'
}

const fetchEmployees = async () => {
  try {
    const res = await axios.get('/api/employees')
    employees.value = res.data
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  }
}

const fetchOvertimeRequests = async () => {
  try {
    const res = await axios.get('/api/overtime')
    overtimeRequests.value = res.data
  } catch (error) {
    ElMessage.error('获取加班列表失败')
  }
}

const openAddDialog = () => {
  newOvertime.value = {
    employeeId: null,
    startTime: '',
    endTime: '',
    reason: ''
  }
  dialogVisible.value = true
}

const submitOvertime = async () => {
  if (!newOvertime.value.employeeId || !newOvertime.value.startTime || !newOvertime.value.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  const overtimeData = {
    ...newOvertime.value,
    startTime: newOvertime.value.startTime ? new Date(newOvertime.value.startTime).toISOString() : null,
    endTime: newOvertime.value.endTime ? new Date(newOvertime.value.endTime).toISOString() : null
  }
  
  try {
    await axios.post('/api/overtime', overtimeData)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    fetchOvertimeRequests()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const approveOvertime = async (id) => {
  try {
    await axios.post(`/api/overtime/${id}/approve`)
    ElMessage.success('审批通过')
    fetchOvertimeRequests()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const rejectOvertime = async (id) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因'
    }).then(async ({ value }) => {
      await axios.post(`/api/overtime/${id}/reject?rejectReason=${encodeURIComponent(value)}`)
      ElMessage.success('已拒绝')
      fetchOvertimeRequests()
    })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchEmployees()
  fetchOvertimeRequests()
})
</script>

<style scoped>
.overtime-page {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
