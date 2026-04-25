package com.attendance.config;

import com.attendance.entity.Employee;
import com.attendance.entity.Shift;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DataStore dataStore;

    @Override
    public void run(String... args) {
        initializeShifts();
        initializeEmployees();
    }

    private void initializeShifts() {
        Shift morningShift = new Shift();
        morningShift.setId(dataStore.generateShiftId());
        morningShift.setName("早班");
        morningShift.setStartTime(LocalTime.of(8, 30));
        morningShift.setEndTime(LocalTime.of(17, 30));
        morningShift.setIsFlexible(false);
        morningShift.setLateGraceMinutes(10);
        morningShift.setEarlyLeaveGraceMinutes(10);
        morningShift.setLateDeductionPerMinute(BigDecimal.valueOf(5));
        morningShift.setActive(true);
        dataStore.getShifts().put(morningShift.getId(), morningShift);

        Shift afternoonShift = new Shift();
        afternoonShift.setId(dataStore.generateShiftId());
        afternoonShift.setName("中班");
        afternoonShift.setStartTime(LocalTime.of(12, 0));
        afternoonShift.setEndTime(LocalTime.of(21, 0));
        afternoonShift.setIsFlexible(false);
        afternoonShift.setLateGraceMinutes(10);
        afternoonShift.setEarlyLeaveGraceMinutes(10);
        afternoonShift.setLateDeductionPerMinute(BigDecimal.valueOf(5));
        afternoonShift.setActive(true);
        dataStore.getShifts().put(afternoonShift.getId(), afternoonShift);

        Shift nightShift = new Shift();
        nightShift.setId(dataStore.generateShiftId());
        nightShift.setName("晚班");
        nightShift.setStartTime(LocalTime.of(19, 0));
        nightShift.setEndTime(LocalTime.of(5, 0));
        nightShift.setIsFlexible(false);
        nightShift.setLateGraceMinutes(10);
        nightShift.setEarlyLeaveGraceMinutes(10);
        nightShift.setLateDeductionPerMinute(BigDecimal.valueOf(5));
        nightShift.setActive(true);
        dataStore.getShifts().put(nightShift.getId(), nightShift);
    }

    private void initializeEmployees() {
        Employee emp1 = new Employee();
        emp1.setId(dataStore.generateEmployeeId());
        emp1.setName("张三");
        emp1.setDepartment("技术部");
        emp1.setPosition("高级工程师");
        emp1.setSalaryStandard(BigDecimal.valueOf(15000));
        emp1.setAnnualLeaveBalance(15);
        emp1.setJoinDate(LocalDate.of(2020, 1, 1));
        emp1.setPhone("13800138001");
        emp1.setEmail("zhangsan@example.com");
        emp1.setActive(true);
        dataStore.getEmployees().put(emp1.getId(), emp1);

        Employee emp2 = new Employee();
        emp2.setId(dataStore.generateEmployeeId());
        emp2.setName("李四");
        emp2.setDepartment("市场部");
        emp2.setPosition("市场经理");
        emp2.setSalaryStandard(BigDecimal.valueOf(12000));
        emp2.setAnnualLeaveBalance(12);
        emp2.setJoinDate(LocalDate.of(2021, 3, 15));
        emp2.setPhone("13800138002");
        emp2.setEmail("lisi@example.com");
        emp2.setActive(true);
        dataStore.getEmployees().put(emp2.getId(), emp2);

        Employee emp3 = new Employee();
        emp3.setId(dataStore.generateEmployeeId());
        emp3.setName("王五");
        emp3.setDepartment("人事部");
        emp3.setPosition("人事专员");
        emp3.setSalaryStandard(BigDecimal.valueOf(8000));
        emp3.setAnnualLeaveBalance(10);
        emp3.setJoinDate(LocalDate.of(2022, 6, 1));
        emp3.setPhone("13800138003");
        emp3.setEmail("wangwu@example.com");
        emp3.setActive(true);
        dataStore.getEmployees().put(emp3.getId(), emp3);
    }
}
