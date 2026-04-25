package com.attendance.controller;

import com.attendance.entity.OvertimeRequest;
import com.attendance.service.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/overtime")
@CrossOrigin(origins = "*")
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;

    @GetMapping
    public List<OvertimeRequest> getAllOvertimeRequests() {
        return overtimeService.getAllOvertimeRequests();
    }

    @GetMapping("/employee/{employeeId}")
    public List<OvertimeRequest> getOvertimeRequestsByEmployee(@PathVariable Long employeeId) {
        return overtimeService.getOvertimeRequestsByEmployee(employeeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OvertimeRequest> getOvertimeRequestById(@PathVariable Long id) {
        OvertimeRequest overtimeRequest = overtimeService.getOvertimeRequestById(id);
        if (overtimeRequest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(overtimeRequest);
    }

    @PostMapping
    public OvertimeRequest createOvertimeRequest(@RequestBody OvertimeRequest overtimeRequest) {
        return overtimeService.createOvertimeRequest(overtimeRequest);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<OvertimeRequest> approveOvertime(
            @PathVariable Long id,
            @RequestParam(required = false) String approver) {
        OvertimeRequest approved = overtimeService.approveOvertime(id, approver != null ? approver : "system");
        if (approved == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<OvertimeRequest> rejectOvertime(
            @PathVariable Long id,
            @RequestParam(required = false) String rejectReason) {
        OvertimeRequest rejected = overtimeService.rejectOvertime(id, rejectReason != null ? rejectReason : "未通过审批");
        if (rejected == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rejected);
    }

    @GetMapping("/hours/{employeeId}")
    public double getTotalOvertimeHours(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return overtimeService.getTotalApprovedOvertimeHours(employeeId, year, month);
    }
}
