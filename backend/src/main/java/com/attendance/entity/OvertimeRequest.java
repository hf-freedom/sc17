package com.attendance.entity;

import com.attendance.enums.ApprovalStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OvertimeRequest {
    private Long id;
    private Long employeeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Double hours;
    private ApprovalStatus status;
    private String approver;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private String rejectReason;
}
