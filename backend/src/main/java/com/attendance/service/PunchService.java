package com.attendance.service;

import com.attendance.entity.PunchRecord;
import com.attendance.entity.Schedule;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PunchService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ScheduleService scheduleService;

    public List<PunchRecord> getAllPunchRecords() {
        return dataStore.getAllPunchRecords();
    }

    public List<PunchRecord> getPunchRecordsByEmployeeAndDate(Long employeeId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return dataStore.getAllPunchRecords().stream()
                .filter(p -> p.getEmployeeId().equals(employeeId))
                .filter(p -> !p.getPunchTime().isBefore(startOfDay))
                .filter(p -> p.getPunchTime().isBefore(endOfDay))
                .collect(Collectors.toList());
    }

    public PunchRecord clockIn(Long employeeId, String location, String device) {
        Schedule schedule = scheduleService.getScheduleByEmployeeAndDate(employeeId, LocalDate.now());
        if (schedule == null) {
            throw new RuntimeException("员工今天没有排班");
        }

        List<PunchRecord> todayRecords = getPunchRecordsByEmployeeAndDate(employeeId, LocalDate.now());
        boolean hasClockIn = todayRecords.stream()
                .anyMatch(r -> r.getPunchType() == PunchRecord.PunchType.CLOCK_IN);
        if (hasClockIn) {
            throw new RuntimeException("今天已经打过上班卡了");
        }

        PunchRecord punchRecord = new PunchRecord();
        punchRecord.setId(dataStore.generatePunchRecordId());
        punchRecord.setEmployeeId(employeeId);
        punchRecord.setPunchTime(LocalDateTime.now());
        punchRecord.setPunchType(PunchRecord.PunchType.CLOCK_IN);
        punchRecord.setLocation(location);
        punchRecord.setDevice(device);

        dataStore.getPunchRecords().put(punchRecord.getId(), punchRecord);
        attendanceService.recalculateAttendance(employeeId, LocalDate.now());

        return punchRecord;
    }

    public PunchRecord clockOut(Long employeeId, String location, String device) {
        Schedule schedule = scheduleService.getScheduleByEmployeeAndDate(employeeId, LocalDate.now());
        if (schedule == null) {
            throw new RuntimeException("员工今天没有排班");
        }

        List<PunchRecord> todayRecords = getPunchRecordsByEmployeeAndDate(employeeId, LocalDate.now());
        boolean hasClockIn = todayRecords.stream()
                .anyMatch(r -> r.getPunchType() == PunchRecord.PunchType.CLOCK_IN);
        if (!hasClockIn) {
            throw new RuntimeException("请先打上班卡");
        }

        boolean hasClockOut = todayRecords.stream()
                .anyMatch(r -> r.getPunchType() == PunchRecord.PunchType.CLOCK_OUT);
        if (hasClockOut) {
            throw new RuntimeException("今天已经打过下班卡了");
        }

        PunchRecord punchRecord = new PunchRecord();
        punchRecord.setId(dataStore.generatePunchRecordId());
        punchRecord.setEmployeeId(employeeId);
        punchRecord.setPunchTime(LocalDateTime.now());
        punchRecord.setPunchType(PunchRecord.PunchType.CLOCK_OUT);
        punchRecord.setLocation(location);
        punchRecord.setDevice(device);

        dataStore.getPunchRecords().put(punchRecord.getId(), punchRecord);
        attendanceService.recalculateAttendance(employeeId, LocalDate.now());

        return punchRecord;
    }

    public PunchRecord getLastPunchByEmployeeAndDate(Long employeeId, LocalDate date) {
        List<PunchRecord> records = getPunchRecordsByEmployeeAndDate(employeeId, date);
        if (records.isEmpty()) {
            return null;
        }
        records.sort((a, b) -> b.getPunchTime().compareTo(a.getPunchTime()));
        return records.get(0);
    }
}
