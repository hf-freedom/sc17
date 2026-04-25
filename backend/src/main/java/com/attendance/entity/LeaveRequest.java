package com.attendance.entity;

import com.attendance.enums.ApprovalStatus;
import com.attendance.enums.LeaveType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveRequest {
    private Long id;
    private Long employeeId;
    private LeaveType leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private ApprovalStatus status;
    private String approver;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private String rejectReason;
}
