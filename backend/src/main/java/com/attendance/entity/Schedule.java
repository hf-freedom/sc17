package com.attendance.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Schedule {
    private Long id;
    private Long employeeId;
    private Long shiftId;
    private LocalDate date;
    private String note;
}
