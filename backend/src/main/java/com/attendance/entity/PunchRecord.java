package com.attendance.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PunchRecord {
    private Long id;
    private Long employeeId;
    private LocalDateTime punchTime;
    private PunchType punchType;
    private String location;
    private String device;

    public enum PunchType {
        CLOCK_IN,
        CLOCK_OUT
    }
}
