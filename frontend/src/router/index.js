import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import EmployeeList from '../views/EmployeeList.vue'
import Schedule from '../views/Schedule.vue'
import Punch from '../views/Punch.vue'
import Leave from '../views/Leave.vue'
import Overtime from '../views/Overtime.vue'
import Salary from '../views/Salary.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/employees',
    name: 'Employees',
    component: EmployeeList
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: Schedule
  },
  {
    path: '/punch',
    name: 'Punch',
    component: Punch
  },
  {
    path: '/leave',
    name: 'Leave',
    component: Leave
  },
  {
    path: '/overtime',
    name: 'Overtime',
    component: Overtime
  },
  {
    path: '/salary',
    name: 'Salary',
    component: Salary
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
