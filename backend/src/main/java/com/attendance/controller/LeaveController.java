package com.attendance.controller;

import com.attendance.entity.LeaveRequest;
import com.attendance.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveService.getAllLeaveRequests();
    }

    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        return leaveService.getLeaveRequestsByEmployee(employeeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveService.getLeaveRequestById(id);
        if (leaveRequest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leaveRequest);
    }

    @PostMapping
    public LeaveRequest createLeaveRequest(@RequestBody LeaveRequest leaveRequest) {
        return leaveService.createLeaveRequest(leaveRequest);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String approver) {
        LeaveRequest approved = leaveService.approveLeave(id, approver != null ? approver : "system");
        if (approved == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String rejectReason) {
        LeaveRequest rejected = leaveService.rejectLeave(id, rejectReason != null ? rejectReason : "未通过审批");
        if (rejected == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rejected);
    }
}
