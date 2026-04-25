<template>
  <div class="leave-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>请假申请</span>
          <el-button type="primary" @click="openAddDialog">新增申请</el-button>
        </div>
      </template>
      
      <el-table :data="leaveRequests" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeId" label="员工ID" width="80" />
        <el-table-column label="员工姓名" width="100">
          <template #default="{ row }">
            {{ getEmployeeName(row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="{ row }">
            {{ getLeaveTypeText(row.leaveType) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="reason" label="请假原因" min-width="150" />
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
              @click="approveLeave(row.id)"
            >
              审批通过
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="danger" 
              size="small" 
              @click="rejectLeave(row.id)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="申请请假" width="500px">
      <el-form :model="newLeave" label-width="100px">
        <el-form-item label="员工">
          <el-select v-model="newLeave.employeeId" placeholder="请选择员工" style="width: 100%">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="`${emp.name} (年假余额: ${emp.annualLeaveBalance}天)`"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="请假类型">
          <el-select v-model="newLeave.leaveType" placeholder="请选择类型" style="width: 100%">
            <el-option label="年假" value="ANNUAL" />
            <el-option label="病假" value="SICK" />
            <el-option label="事假" value="PERSONAL" />
            <el-option label="产假" value="MATERNITY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="newLeave.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="newLeave.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="请假原因">
          <el-input v-model="newLeave.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLeave">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const employees = ref([])
const leaveRequests = ref([])
const dialogVisible = ref(false)
const newLeave = ref({
  employeeId: null,
  leaveType: '',
  startTime: '',
  endTime: '',
  reason: ''
})

const getEmployeeName = (employeeId) => {
  const emp = employees.value.find(e => e.id === employeeId)
  return emp ? emp.name : '未知'
}

const getLeaveTypeText = (type) => {
  const types = {
    ANNUAL: '年假',
    SICK: '病假',
    PERSONAL: '事假',
    MATERNITY: '产假',
    OTHER: '其他'
  }
  return types[type] || type
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

const fetchLeaveRequests = async () => {
  try {
    const res = await axios.get('/api/leaves')
    leaveRequests.value = res.data
  } catch (error) {
    ElMessage.error('获取请假列表失败')
  }
}

const openAddDialog = () => {
  newLeave.value = {
    employeeId: null,
    leaveType: '',
    startTime: '',
    endTime: '',
    reason: ''
  }
  dialogVisible.value = true
}

const submitLeave = async () => {
  if (!newLeave.value.employeeId || !newLeave.value.leaveType || !newLeave.value.startTime || !newLeave.value.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  const leaveData = {
    ...newLeave.value,
    startTime: newLeave.value.startTime ? new Date(newLeave.value.startTime).toISOString() : null,
    endTime: newLeave.value.endTime ? new Date(newLeave.value.endTime).toISOString() : null
  }
  
  try {
    await axios.post('/api/leaves', leaveData)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    fetchLeaveRequests()
    fetchEmployees()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '提交失败')
  }
}

const approveLeave = async (id) => {
  try {
    await axios.post(`/api/leaves/${id}/approve`)
    ElMessage.success('审批通过')
    fetchLeaveRequests()
    fetchEmployees()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const rejectLeave = async (id) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因'
    }).then(async ({ value }) => {
      await axios.post(`/api/leaves/${id}/reject?rejectReason=${encodeURIComponent(value)}`)
      ElMessage.success('已拒绝')
      fetchLeaveRequests()
    })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchEmployees()
  fetchLeaveRequests()
})
</script>

<style scoped>
.leave-page {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
