package com.attendance.repository;

import com.attendance.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DataStore {
    private final AtomicLong employeeIdGenerator = new AtomicLong(1);
    private final AtomicLong shiftIdGenerator = new AtomicLong(1);
    private final AtomicLong scheduleIdGenerator = new AtomicLong(1);
    private final AtomicLong punchRecordIdGenerator = new AtomicLong(1);
    private final AtomicLong leaveRequestIdGenerator = new AtomicLong(1);
    private final AtomicLong overtimeRequestIdGenerator = new AtomicLong(1);
    private final AtomicLong attendanceRecordIdGenerator = new AtomicLong(1);
    private final AtomicLong salaryRecordIdGenerator = new AtomicLong(1);

    private final ConcurrentHashMap<Long, Employee> employees = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Shift> shifts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Schedule> schedules = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, PunchRecord> punchRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, LeaveRequest> leaveRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, OvertimeRequest> overtimeRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AttendanceRecord> attendanceRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, SalaryRecord> salaryRecords = new ConcurrentHashMap<>();

    public long generateEmployeeId() {
        return employeeIdGenerator.getAndIncrement();
    }

    public long generateShiftId() {
        return shiftIdGenerator.getAndIncrement();
    }

    public long generateScheduleId() {
        return scheduleIdGenerator.getAndIncrement();
    }

    public long generatePunchRecordId() {
        return punchRecordIdGenerator.getAndIncrement();
    }

    public long generateLeaveRequestId() {
        return leaveRequestIdGenerator.getAndIncrement();
    }

    public long generateOvertimeRequestId() {
        return overtimeRequestIdGenerator.getAndIncrement();
    }

    public long generateAttendanceRecordId() {
        return attendanceRecordIdGenerator.getAndIncrement();
    }

    public long generateSalaryRecordId() {
        return salaryRecordIdGenerator.getAndIncrement();
    }

    public ConcurrentHashMap<Long, Employee> getEmployees() {
        return employees;
    }

    public ConcurrentHashMap<Long, Shift> getShifts() {
        return shifts;
    }

    public ConcurrentHashMap<Long, Schedule> getSchedules() {
        return schedules;
    }

    public ConcurrentHashMap<Long, PunchRecord> getPunchRecords() {
        return punchRecords;
    }

    public ConcurrentHashMap<Long, LeaveRequest> getLeaveRequests() {
        return leaveRequests;
    }

    public ConcurrentHashMap<Long, OvertimeRequest> getOvertimeRequests() {
        return overtimeRequests;
    }

    public ConcurrentHashMap<Long, AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }

    public ConcurrentHashMap<Long, SalaryRecord> getSalaryRecords() {
        return salaryRecords;
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    public List<Shift> getAllShifts() {
        return new ArrayList<>(shifts.values());
    }

    public List<Schedule> getAllSchedules() {
        return new ArrayList<>(schedules.values());
    }

    public List<PunchRecord> getAllPunchRecords() {
        return new ArrayList<>(punchRecords.values());
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return new ArrayList<>(leaveRequests.values());
    }

    public List<OvertimeRequest> getAllOvertimeRequests() {
        return new ArrayList<>(overtimeRequests.values());
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return new ArrayList<>(attendanceRecords.values());
    }

    public List<SalaryRecord> getAllSalaryRecords() {
        return new ArrayList<>(salaryRecords.values());
    }
}
