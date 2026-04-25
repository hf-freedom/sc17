package com.attendance.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Employee {
    private Long id;
    private String name;
    private String department;
    private String position;
    private BigDecimal salaryStandard;
    private Integer annualLeaveBalance;
    private LocalDate joinDate;
    private String phone;
    private String email;
    private Boolean active;
}
