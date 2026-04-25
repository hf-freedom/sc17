package com.attendance.entity;

import com.attendance.enums.AttendanceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AttendanceRecord {
    private Long id;
    private Long employeeId;
    private LocalDate date;
    private Long shiftId;
    private LocalTime actualInTime;
    private LocalTime actualOutTime;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private BigDecimal lateDeduction;
    private AttendanceStatus status;
    private List<LeaveSegment> leaveSegments = new ArrayList<>();
    private List<WorkSegment> workSegments = new ArrayList<>();
    private String remark;

    @Data
    public static class LeaveSegment {
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer minutes;
        private String leaveType;
    }

    @Data
    public static class WorkSegment {
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer minutes;
        private Boolean isNormal;
    }
}
