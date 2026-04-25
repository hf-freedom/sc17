<template>
  <div class="home-page">
    <el-card>
      <template #header>
        <span>欢迎使用员工考勤薪资系统</span>
      </template>
      <div class="welcome-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="系统功能">
            <ul>
              <li>员工管理 - 管理员工信息、薪资标准、年假余额</li>
              <li>排班管理 - 为员工安排班次</li>
              <li>打卡管理 - 上下班打卡，自动计算迟到早退</li>
              <li>请假管理 - 申请请假，审批后扣除假期</li>
              <li>加班管理 - 申请加班，审批后计入加班时长</li>
              <li>薪资管理 - 根据考勤计算工资，确认后锁定数据</li>
            </ul>
          </el-descriptions-item>
          <el-descriptions-item label="默认数据">
            <ul>
              <li>3个默认员工：张三、李四、王五</li>
              <li>3个默认班次：早班、中班、晚班</li>
            </ul>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-page {
  max-width: 800px;
  margin: 0 auto;
}

.welcome-content {
  line-height: 1.8;
}

ul {
  margin: 0;
  padding-left: 20px;
}

li {
  margin-bottom: 5px;
}
</style>
