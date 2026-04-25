package com.attendance.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class Shift {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isFlexible;
    private Integer lateGraceMinutes;
    private Integer earlyLeaveGraceMinutes;
    private BigDecimal lateDeductionPerMinute;
    private Boolean active;
}
