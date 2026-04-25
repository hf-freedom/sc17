<template>
  <div class="salary-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>薪资管理</span>
          <div class="header-actions">
            <el-select v-model="selectedYear" placeholder="选择年份" style="width: 120px;">
              <el-option :value="2024" label="2024年" />
              <el-option :value="2025" label="2025年" />
              <el-option :value="2026" label="2026年" />
            </el-select>
            <el-select v-model="selectedMonth" placeholder="选择月份" style="width: 120px;">
              <el-option v-for="m in 12" :key="m" :value="m" :label="`${m}月`" />
            </el-select>
            <el-button type="primary" @click="generateMonthlySalary">
              生成当月薪资
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="salaryRecords" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeId" label="员工ID" width="80" />
        <el-table-column label="员工姓名" width="100">
          <template #default="{ row }">
            {{ getEmployeeName(row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column label="年月" width="100">
          <template #default="{ row }">
            {{ row.year }}-{{ row.month }}
          </template>
        </el-table-column>
        <el-table-column prop="baseSalary" label="基本工资" width="120">
          <template #default="{ row }">
            ¥{{ row.baseSalary }}
          </template>
        </el-table-column>
        <el-table-column prop="totalLateDeduction" label="迟到扣款" width="100">
          <template #default="{ row }">
            <span style="color: red;">-¥{{ row.totalLateDeduction }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAbsentDeduction" label="缺勤扣款" width="100">
          <template #default="{ row }">
            <span style="color: red;">-¥{{ row.totalAbsentDeduction }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSickDeduction" label="病假扣款" width="100">
          <template #default="{ row }">
            <span style="color: red;">-¥{{ row.totalSickDeduction }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalOvertimePay" label="加班费" width="100">
          <template #default="{ row }">
            <span style="color: green;">+¥{{ row.totalOvertimePay }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="netSalary" label="实发工资" width="120">
          <template #default="{ row }">
            <span style="font-weight: bold; color: #409eff;">¥{{ row.netSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="confirmed" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.confirmed ? 'success' : 'warning'">
              {{ row.confirmed ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              v-if="!row.confirmed" 
              type="success" 
              size="small" 
              @click="confirmSalary(row.id)"
            >
              确认薪资
            </el-button>
            <el-button 
              v-if="row.confirmed" 
              type="warning" 
              size="small" 
              @click="openAdjustDialog(row)"
            >
              薪资调整
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="adjustDialogVisible" title="薪资调整" width="400px">
      <el-form :model="adjustForm" label-width="80px">
        <el-form-item label="调整金额">
          <el-input-number v-model="adjustForm.amount" :precision="2" style="width: 100%;" />
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            正数为增加，负数为扣除
          </div>
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjustment">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const employees = ref([])
const salaryRecords = ref([])
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const adjustDialogVisible = ref(false)
const adjustForm = ref({
  salaryId: null,
  amount: 0,
  reason: ''
})

const getEmployeeName = (employeeId) => {
  const emp = employees.value.find(e => e.id === employeeId)
  return emp ? emp.name : '未知'
}

const fetchEmployees = async () => {
  try {
    const res = await axios.get('/api/employees')
    employees.value = res.data
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  }
}

const fetchSalaryRecords = async () => {
  try {
    const res = await axios.get('/api/salary')
    salaryRecords.value = res.data.filter(s => s.year === selectedYear.value && s.month === selectedMonth.value)
  } catch (error) {
    ElMessage.error('获取薪资列表失败')
  }
}

const generateMonthlySalary = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要生成 ${selectedYear.value}年${selectedMonth.value}月 的薪资数据吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await axios.post(`/api/salary/generate/monthly?year=${selectedYear.value}&month=${selectedMonth.value}`)
    ElMessage.success('薪资生成成功')
    fetchSalaryRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '生成失败')
    }
  }
}

const confirmSalary = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确认薪资后，考勤数据将被锁定，只能通过薪资调整单修改，确定继续吗？',
      '确认薪资',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await axios.post(`/api/salary/${id}/confirm`)
    ElMessage.success('薪资已确认')
    fetchSalaryRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const openAdjustDialog = (row) => {
  adjustForm.value = {
    salaryId: row.id,
    amount: 0,
    reason: ''
  }
  adjustDialogVisible.value = true
}

const submitAdjustment = async () => {
  if (!adjustForm.value.reason) {
    ElMessage.warning('请填写调整原因')
    return
  }
  try {
    await axios.post(
      `/api/salary/${adjustForm.value.salaryId}/adjustment?amount=${adjustForm.value.amount}&reason=${encodeURIComponent(adjustForm.value.reason)}`
    )
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    fetchSalaryRecords()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}

onMounted(() => {
  fetchEmployees()
  fetchSalaryRecords()
})
</script>

<style scoped>
.salary-page {
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
