package com.attendance.service;

import com.attendance.entity.*;
import com.attendance.enums.AttendanceStatus;
import com.attendance.enums.LeaveType;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaryService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private OvertimeService overtimeService;

    private static final BigDecimal SICK_LEAVE_DEDUCTION_RATE = BigDecimal.valueOf(0.5);
    private static final BigDecimal WORKING_DAYS_PER_MONTH = BigDecimal.valueOf(21.75);
    private static final BigDecimal MINUTES_PER_DAY = BigDecimal.valueOf(480);
    private static final BigDecimal LATE_DEDUCTION_MULTIPLIER = BigDecimal.valueOf(1.5);

    public List<SalaryRecord> getAllSalaryRecords() {
        return dataStore.getAllSalaryRecords();
    }

    public List<SalaryRecord> getSalaryRecordsByEmployee(Long employeeId) {
        return dataStore.getAllSalaryRecords().stream()
                .filter(s -> s.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public SalaryRecord getSalaryRecordById(Long id) {
        return dataStore.getSalaryRecords().get(id);
    }

    public SalaryRecord getSalaryRecordByEmployeeAndMonth(Long employeeId, int year, int month) {
        return dataStore.getAllSalaryRecords().stream()
                .filter(s -> s.getEmployeeId().equals(employeeId))
                .filter(s -> s.getYear() == year)
                .filter(s -> s.getMonth() == month)
                .findFirst()
                .orElse(null);
    }

    public SalaryRecord generateSalary(Long employeeId, int year, int month) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        SalaryRecord existing = getSalaryRecordByEmployeeAndMonth(employeeId, year, month);
        if (existing != null && existing.getConfirmed()) {
            throw new RuntimeException("该月薪资已确认，无法重新生成");
        }

        SalaryRecord salaryRecord = existing != null ? existing : new SalaryRecord();
        if (existing == null) {
            salaryRecord.setId(dataStore.generateSalaryRecordId());
        }

        salaryRecord.setEmployeeId(employeeId);
        salaryRecord.setYear(year);
        salaryRecord.setMonth(month);
        salaryRecord.setBaseSalary(employee.getSalaryStandard());
        salaryRecord.setCreateTime(LocalDate.now());

        List<AttendanceRecord> attendanceRecords = attendanceService.getAttendanceByEmployeeAndMonth(employeeId, year, month);
        List<SalaryRecord.AttendanceSummary> summaries = attendanceRecords.stream()
                .map(this::convertToSummary)
                .collect(Collectors.toList());
        salaryRecord.setAttendanceSummary(summaries);

        BigDecimal totalLateDeduction = calculateTotalLateDeduction(attendanceRecords, employee.getSalaryStandard());
        BigDecimal totalAbsentDeduction = calculateTotalAbsentDeduction(attendanceRecords, employee.getSalaryStandard());
        BigDecimal totalSickDeduction = calculateTotalSickDeduction(attendanceRecords, employee.getSalaryStandard());
        double overtimeHours = overtimeService.getTotalApprovedOvertimeHours(employeeId, year, month);
        BigDecimal totalOvertimePay = calculateOvertimePay(overtimeHours, employee.getSalaryStandard());

        salaryRecord.setTotalLateDeduction(totalLateDeduction);
        salaryRecord.setTotalAbsentDeduction(totalAbsentDeduction);
        salaryRecord.setTotalSickDeduction(totalSickDeduction);
        salaryRecord.setTotalOvertimePay(totalOvertimePay);
        salaryRecord.setTotalAllowance(BigDecimal.ZERO);

        BigDecimal netSalary = employee.getSalaryStandard()
                .subtract(totalLateDeduction)
                .subtract(totalAbsentDeduction)
                .subtract(totalSickDeduction)
                .add(totalOvertimePay);
        salaryRecord.setNetSalary(netSalary);

        if (existing == null) {
            salaryRecord.setConfirmed(false);
        }

        dataStore.getSalaryRecords().put(salaryRecord.getId(), salaryRecord);
        return salaryRecord;
    }

    public SalaryRecord confirmSalary(Long id, String confirmBy) {
        SalaryRecord salaryRecord = dataStore.getSalaryRecords().get(id);
        if (salaryRecord == null) {
            return null;
        }
        if (salaryRecord.getConfirmed()) {
            throw new RuntimeException("薪资已确认，无需重复确认");
        }

        salaryRecord.setConfirmed(true);
        salaryRecord.setConfirmDate(LocalDate.now());
        salaryRecord.setConfirmBy(confirmBy);
        dataStore.getSalaryRecords().put(id, salaryRecord);

        return salaryRecord;
    }

    public SalaryRecord addSalaryAdjustment(Long id, BigDecimal amount, String reason, String adjustBy) {
        SalaryRecord salaryRecord = dataStore.getSalaryRecords().get(id);
        if (salaryRecord == null) {
            return null;
        }

        SalaryRecord.SalaryAdjustment adjustment = new SalaryRecord.SalaryAdjustment();
        adjustment.setId(System.currentTimeMillis());
        adjustment.setAmount(amount);
        adjustment.setReason(reason);
        adjustment.setAdjustDate(LocalDate.now());
        adjustment.setAdjustBy(adjustBy);

        salaryRecord.getAdjustments().add(adjustment);

        BigDecimal netSalary = salaryRecord.getNetSalary().add(amount);
        salaryRecord.setNetSalary(netSalary);

        dataStore.getSalaryRecords().put(id, salaryRecord);
        return salaryRecord;
    }

    public boolean isAttendanceLocked(Long employeeId, LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        SalaryRecord salaryRecord = getSalaryRecordByEmployeeAndMonth(employeeId, year, month);
        return salaryRecord != null && salaryRecord.getConfirmed();
    }

    private SalaryRecord.AttendanceSummary convertToSummary(AttendanceRecord record) {
        SalaryRecord.AttendanceSummary summary = new SalaryRecord.AttendanceSummary();
        summary.setDate(record.getDate());
        summary.setStatus(record.getStatus() != null ? record.getStatus().name() : "UNKNOWN");
        summary.setLateMinutes(record.getLateMinutes() != null ? record.getLateMinutes() : 0);
        summary.setEarlyLeaveMinutes(record.getEarlyLeaveMinutes() != null ? record.getEarlyLeaveMinutes() : 0);
        summary.setDeduction(record.getLateDeduction() != null ? record.getLateDeduction() : BigDecimal.ZERO);
        return summary;
    }

    private BigDecimal calculateDailySalary(BigDecimal baseSalary) {
        return baseSalary.divide(WORKING_DAYS_PER_MONTH, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMinuteSalary(BigDecimal baseSalary) {
        return calculateDailySalary(baseSalary).divide(MINUTES_PER_DAY, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalLateDeduction(List<AttendanceRecord> records, BigDecimal baseSalary) {
        int totalLateMinutes = records.stream()
                .mapToInt(r -> {
                    int late = r.getLateMinutes() != null ? r.getLateMinutes() : 0;
                    int early = r.getEarlyLeaveMinutes() != null ? r.getEarlyLeaveMinutes() : 0;
                    return late + early;
                })
                .sum();

        if (totalLateMinutes == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal minuteSalary = calculateMinuteSalary(baseSalary);
        return minuteSalary
                .multiply(BigDecimal.valueOf(totalLateMinutes))
                .multiply(LATE_DEDUCTION_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalAbsentDeduction(List<AttendanceRecord> records, BigDecimal baseSalary) {
        long absentDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT)
                .count();

        if (absentDays == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailySalary = calculateDailySalary(baseSalary);
        return dailySalary
                .multiply(BigDecimal.valueOf(absentDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalSickDeduction(List<AttendanceRecord> records, BigDecimal baseSalary) {
        int totalSickMinutes = records.stream()
                .flatMap(r -> r.getLeaveSegments().stream())
                .filter(s -> LeaveType.SICK.name().equals(s.getLeaveType()))
                .mapToInt(AttendanceRecord.LeaveSegment::getMinutes)
                .sum();

        if (totalSickMinutes == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal minuteSalary = calculateMinuteSalary(baseSalary);
        return minuteSalary
                .multiply(BigDecimal.valueOf(totalSickMinutes))
                .multiply(SICK_LEAVE_DEDUCTION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOvertimePay(double hours, BigDecimal baseSalary) {
        if (hours <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal hourlyRate = baseSalary.divide(BigDecimal.valueOf(21.75), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(8), 4, RoundingMode.HALF_UP);

        return hourlyRate
                .multiply(BigDecimal.valueOf(1.5))
                .multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public List<SalaryRecord> generateMonthlySalary(int year, int month) {
        List<Employee> employees = employeeService.getAllEmployees();
        for (Employee employee : employees) {
            try {
                generateSalary(employee.getId(), year, month);
            } catch (Exception e) {
                System.out.println("生成员工 " + employee.getName() + " 薪资失败: " + e.getMessage());
            }
        }
        return dataStore.getAllSalaryRecords().stream()
                .filter(s -> s.getYear() == year && s.getMonth() == month)
                .collect(Collectors.toList());
    }
}
