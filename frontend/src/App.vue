<template>
  <el-container class="app-container">
    <el-header>
      <h2>员工考勤薪资系统</h2>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/employees">
            <el-icon><User /></el-icon>
            <span>员工列表</span>
          </el-menu-item>
          <el-menu-item index="/schedule">
            <el-icon><Calendar /></el-icon>
            <span>排班管理</span>
          </el-menu-item>
          <el-menu-item index="/punch">
            <el-icon><Clock /></el-icon>
            <span>打卡</span>
          </el-menu-item>
          <el-menu-item index="/leave">
            <el-icon><Document /></el-icon>
            <span>请假申请</span>
          </el-menu-item>
          <el-menu-item index="/overtime">
            <el-icon><Operation /></el-icon>
            <span>加班申请</span>
          </el-menu-item>
          <el-menu-item index="/salary">
            <el-icon><Money /></el-icon>
            <span>薪资管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeFilled,
  User,
  Calendar,
  Clock,
  Document,
  Operation,
  Money
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-container {
  height: 100vh;
}

.el-header {
  background-color: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.el-aside {
  background-color: #545c64;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
