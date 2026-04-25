<template>
  <div class="employee-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>员工列表</span>
          <el-button type="primary" @click="openAddDialog">添加员工</el-button>
        </div>
      </template>
      
      <el-table :data="employees" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="岗位" width="120" />
        <el-table-column prop="salaryStandard" label="薪资标准" width="120">
          <template #default="{ row }">
            ¥{{ row.salaryStandard }}
          </template>
        </el-table-column>
        <el-table-column prop="annualLeaveBalance" label="年假余额" width="100">
          <template #default="{ row }">
            {{ row.annualLeaveBalance }} 天
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="joinDate" label="入职日期" width="120" />
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="添加员工" width="500px">
      <el-form :model="newEmployee" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="newEmployee.name" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="newEmployee.department" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="newEmployee.position" />
        </el-form-item>
        <el-form-item label="薪资标准">
          <el-input-number v-model="newEmployee.salaryStandard" :min="0" />
        </el-form-item>
        <el-form-item label="年假余额">
          <el-input-number v-model="newEmployee.annualLeaveBalance" :min="0" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="newEmployee.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="newEmployee.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addEmployee">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const employees = ref([])
const dialogVisible = ref(false)
const newEmployee = ref({
  name: '',
  department: '',
  position: '',
  salaryStandard: 8000,
  annualLeaveBalance: 10,
  phone: '',
  email: ''
})

const fetchEmployees = async () => {
  try {
    const res = await axios.get('/api/employees')
    employees.value = res.data
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  }
}

const openAddDialog = () => {
  newEmployee.value = {
    name: '',
    department: '',
    position: '',
    salaryStandard: 8000,
    annualLeaveBalance: 10,
    phone: '',
    email: ''
  }
  dialogVisible.value = true
}

const addEmployee = async () => {
  try {
    await axios.post('/api/employees', newEmployee.value)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchEmployees()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

onMounted(() => {
  fetchEmployees()
})
</script>

<style scoped>
.employee-page {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
