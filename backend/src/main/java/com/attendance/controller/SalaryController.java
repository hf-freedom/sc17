package com.attendance.controller;

import com.attendance.entity.SalaryRecord;
import com.attendance.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "*")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    @GetMapping
    public List<SalaryRecord> getAllSalaryRecords() {
        return salaryService.getAllSalaryRecords();
    }

    @GetMapping("/employee/{employeeId}")
    public List<SalaryRecord> getSalaryRecordsByEmployee(@PathVariable Long employeeId) {
        return salaryService.getSalaryRecordsByEmployee(employeeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryRecord> getSalaryRecordById(@PathVariable Long id) {
        SalaryRecord salaryRecord = salaryService.getSalaryRecordById(id);
        if (salaryRecord == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(salaryRecord);
    }

    @GetMapping("/employee/{employeeId}/month")
    public ResponseEntity<SalaryRecord> getSalaryRecordByMonth(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        SalaryRecord salaryRecord = salaryService.getSalaryRecordByEmployeeAndMonth(employeeId, year, month);
        if (salaryRecord == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(salaryRecord);
    }

    @PostMapping("/generate")
    public SalaryRecord generateSalary(
            @RequestParam Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return salaryService.generateSalary(employeeId, year, month);
    }

    @PostMapping("/generate/monthly")
    public List<SalaryRecord> generateMonthlySalary(
            @RequestParam int year,
            @RequestParam int month) {
        return salaryService.generateMonthlySalary(year, month);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SalaryRecord> confirmSalary(
            @PathVariable Long id,
            @RequestParam(required = false) String confirmBy) {
        SalaryRecord confirmed = salaryService.confirmSalary(id, confirmBy != null ? confirmBy : "system");
        if (confirmed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(confirmed);
    }

    @PostMapping("/{id}/adjustment")
    public ResponseEntity<SalaryRecord> addAdjustment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String reason,
            @RequestParam(required = false) String adjustBy) {
        SalaryRecord adjusted = salaryService.addSalaryAdjustment(
                id, amount, reason, adjustBy != null ? adjustBy : "system");
        if (adjusted == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(adjusted);
    }
}
