package com.attendance.service;

import com.attendance.entity.Employee;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private DataStore dataStore;

    public List<Employee> getAllEmployees() {
        return dataStore.getAllEmployees().stream()
                .filter(Employee::getActive)
                .collect(Collectors.toList());
    }

    public Employee getEmployeeById(Long id) {
        return dataStore.getEmployees().get(id);
    }

    public Employee createEmployee(Employee employee) {
        employee.setId(dataStore.generateEmployeeId());
        if (employee.getActive() == null) {
            employee.setActive(true);
        }
        dataStore.getEmployees().put(employee.getId(), employee);
        return employee;
    }

    public Employee updateEmployee(Long id, Employee employee) {
        Employee existing = dataStore.getEmployees().get(id);
        if (existing == null) {
            return null;
        }
        employee.setId(id);
        dataStore.getEmployees().put(id, employee);
        return employee;
    }

    public boolean deleteEmployee(Long id) {
        Employee employee = dataStore.getEmployees().get(id);
        if (employee == null) {
            return false;
        }
        employee.setActive(false);
        dataStore.getEmployees().put(id, employee);
        return true;
    }

    public Employee updateAnnualLeaveBalance(Long employeeId, int delta) {
        Employee employee = dataStore.getEmployees().get(employeeId);
        if (employee == null) {
            return null;
        }
        int newBalance = employee.getAnnualLeaveBalance() + delta;
        if (newBalance < 0) {
            return null;
        }
        employee.setAnnualLeaveBalance(newBalance);
        dataStore.getEmployees().put(employeeId, employee);
        return employee;
    }
}
