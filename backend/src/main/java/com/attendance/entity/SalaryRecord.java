package com.attendance.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class SalaryRecord {
    private Long id;
    private Long employeeId;
    private Integer year;
    private Integer month;
    private BigDecimal baseSalary;
    private BigDecimal totalLateDeduction;
    private BigDecimal totalAbsentDeduction;
    private BigDecimal totalSickDeduction;
    private BigDecimal totalOvertimePay;
    private BigDecimal totalAllowance;
    private BigDecimal netSalary;
    private Boolean confirmed;
    private LocalDate confirmDate;
    private String confirmBy;
    private List<SalaryAdjustment> adjustments = new ArrayList<>();
    private List<AttendanceSummary> attendanceSummary = new ArrayList<>();
    private LocalDate createTime;

    @Data
    public static class SalaryAdjustment {
        private Long id;
        private BigDecimal amount;
        private String reason;
        private LocalDate adjustDate;
        private String adjustBy;
    }

    @Data
    public static class AttendanceSummary {
        private LocalDate date;
        private String status;
        private Integer lateMinutes;
        private Integer earlyLeaveMinutes;
        private BigDecimal deduction;
    }
}
