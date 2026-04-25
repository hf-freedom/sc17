package com.attendance.service;

import com.attendance.entity.*;
import com.attendance.enums.ApprovalStatus;
import com.attendance.enums.AttendanceStatus;
import com.attendance.enums.LeaveType;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private DataStore dataStore;

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return dataStore.getAllAttendanceRecords();
    }

    public AttendanceRecord getAttendanceByEmployeeAndDate(Long employeeId, LocalDate date) {
        return dataStore.getAllAttendanceRecords().stream()
                .filter(a -> a.getEmployeeId().equals(employeeId))
                .filter(a -> a.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    public void recalculateAttendance(Long employeeId, LocalDate date) {
        if (isAttendanceLocked(employeeId, date)) {
            return;
        }

        AttendanceRecord existing = getAttendanceByEmployeeAndDate(employeeId, date);
        if (existing == null) {
            existing = new AttendanceRecord();
            existing.setId(dataStore.generateAttendanceRecordId());
            existing.setEmployeeId(employeeId);
            existing.setDate(date);
        }

        Schedule schedule = dataStore.getAllSchedules().stream()
                .filter(s -> s.getEmployeeId().equals(employeeId))
                .filter(s -> s.getDate().equals(date))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            existing.setStatus(AttendanceStatus.ABSENT);
            dataStore.getAttendanceRecords().put(existing.getId(), existing);
            return;
        }

        existing.setShiftId(schedule.getShiftId());
        Shift shift = dataStore.getShifts().get(schedule.getShiftId());

        if (shift == null) {
            existing.setStatus(AttendanceStatus.ABSENT);
            dataStore.getAttendanceRecords().put(existing.getId(), existing);
            return;
        }

        List<PunchRecord> punchRecords = getPunchRecordsForDay(employeeId, date);
        List<LeaveRequest> approvedLeaves = getApprovedLeavesForDay(employeeId, date);

        LocalTime startShift = shift.getStartTime();
        LocalTime endShift = shift.getEndTime();

        LocalTime actualInTime = null;
        LocalTime actualOutTime = null;

        for (PunchRecord punch : punchRecords) {
            LocalTime punchTime = punch.getPunchTime().toLocalTime();
            if (punch.getPunchType() == PunchRecord.PunchType.CLOCK_IN) {
                if (actualInTime == null || punchTime.isBefore(actualInTime)) {
                    actualInTime = punchTime;
                }
            } else {
                if (actualOutTime == null || punchTime.isAfter(actualOutTime)) {
                    actualOutTime = punchTime;
                }
            }
        }

        existing.setActualInTime(actualInTime);
        existing.setActualOutTime(actualOutTime);

        List<AttendanceRecord.LeaveSegment> leaveSegments = new ArrayList<>();
        for (LeaveRequest leave : approvedLeaves) {
            LocalDateTime leaveStart = leave.getStartTime();
            LocalDateTime leaveEnd = leave.getEndTime();

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            LocalDateTime effectiveStart = leaveStart.isBefore(dayStart) ? dayStart : leaveStart;
            LocalDateTime effectiveEnd = leaveEnd.isAfter(dayEnd) ? dayEnd : leaveEnd;

            if (effectiveStart.isBefore(effectiveEnd)) {
                AttendanceRecord.LeaveSegment segment = new AttendanceRecord.LeaveSegment();
                segment.setStartTime(effectiveStart.toLocalTime());
                segment.setEndTime(effectiveEnd.toLocalTime());
                segment.setMinutes((int) ChronoUnit.MINUTES.between(effectiveStart, effectiveEnd));
                segment.setLeaveType(leave.getLeaveType().name());
                leaveSegments.add(segment);
            }
        }
        existing.setLeaveSegments(leaveSegments);

        int lateMinutes = 0;
        int earlyLeaveMinutes = 0;

        if (actualInTime != null) {
            int grace = shift.getLateGraceMinutes() != null ? shift.getLateGraceMinutes() : 0;
            LocalTime graceStartTime = startShift.plusMinutes(grace);
            if (actualInTime.isAfter(graceStartTime)) {
                lateMinutes = (int) ChronoUnit.MINUTES.between(startShift, actualInTime) - grace;
                if (lateMinutes < 0) lateMinutes = 0;
            }
        }

        if (actualOutTime != null) {
            int grace = shift.getEarlyLeaveGraceMinutes() != null ? shift.getEarlyLeaveGraceMinutes() : 0;
            LocalTime graceEndTime = endShift.minusMinutes(grace);
            if (actualOutTime.isBefore(graceEndTime)) {
                earlyLeaveMinutes = (int) ChronoUnit.MINUTES.between(actualOutTime, endShift) - grace;
                if (earlyLeaveMinutes < 0) earlyLeaveMinutes = 0;
            }
        }

        existing.setLateMinutes(lateMinutes);
        existing.setEarlyLeaveMinutes(earlyLeaveMinutes);

        BigDecimal deduction = BigDecimal.ZERO;
        BigDecimal perMinute = shift.getLateDeductionPerMinute() != null ? shift.getLateDeductionPerMinute() : BigDecimal.ZERO;
        deduction = perMinute.multiply(BigDecimal.valueOf(lateMinutes + earlyLeaveMinutes));
        existing.setLateDeduction(deduction);

        boolean hasLeave = !leaveSegments.isEmpty();
        boolean hasPunch = actualInTime != null || actualOutTime != null;

        if (hasLeave && hasPunch) {
            List<TimeRange> workRanges = splitByLeave(startShift, endShift, leaveSegments);
            List<AttendanceRecord.WorkSegment> workSegments = new ArrayList<>();

            boolean allNormal = true;
            for (TimeRange range : workRanges) {
                AttendanceRecord.WorkSegment segment = new AttendanceRecord.WorkSegment();
                segment.setStartTime(range.start);
                segment.setEndTime(range.end);
                segment.setMinutes((int) ChronoUnit.MINUTES.between(range.start, range.end));

                boolean segmentPunched = isSegmentPunched(range, actualInTime, actualOutTime);
                segment.setIsNormal(segmentPunched);

                if (!segmentPunched) {
                    allNormal = false;
                }
                workSegments.add(segment);
            }
            existing.setWorkSegments(workSegments);
            existing.setStatus(allNormal ? AttendanceStatus.NORMAL : AttendanceStatus.ABSENT);
        } else if (hasLeave) {
            existing.setStatus(AttendanceStatus.LEAVE);
        } else if (hasPunch) {
            if (lateMinutes > 0 || earlyLeaveMinutes > 0) {
                existing.setStatus(AttendanceStatus.LATE);
            } else {
                existing.setStatus(AttendanceStatus.NORMAL);
            }
        } else {
            existing.setStatus(AttendanceStatus.ABSENT);
        }

        dataStore.getAttendanceRecords().put(existing.getId(), existing);
    }

    private boolean isAttendanceLocked(Long employeeId, LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        return dataStore.getAllSalaryRecords().stream()
                .anyMatch(s -> s.getEmployeeId().equals(employeeId)
                        && s.getYear() == year
                        && s.getMonth() == month
                        && Boolean.TRUE.equals(s.getConfirmed()));
    }

    private List<PunchRecord> getPunchRecordsForDay(Long employeeId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        return dataStore.getAllPunchRecords().stream()
                .filter(p -> p.getEmployeeId().equals(employeeId))
                .filter(p -> !p.getPunchTime().isBefore(dayStart))
                .filter(p -> p.getPunchTime().isBefore(dayEnd))
                .collect(Collectors.toList());
    }

    private List<LeaveRequest> getApprovedLeavesForDay(Long employeeId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        return dataStore.getAllLeaveRequests().stream()
                .filter(l -> l.getEmployeeId().equals(employeeId))
                .filter(l -> l.getStatus() == ApprovalStatus.APPROVED)
                .filter(l -> l.getEndTime().isAfter(dayStart))
                .filter(l -> l.getStartTime().isBefore(dayEnd))
                .collect(Collectors.toList());
    }

    private List<TimeRange> splitByLeave(LocalTime start, LocalTime end, List<AttendanceRecord.LeaveSegment> leaves) {
        List<TimeRange> result = new ArrayList<>();
        if (leaves.isEmpty()) {
            result.add(new TimeRange(start, end));
            return result;
        }

        List<TimeRange> sortedLeaves = leaves.stream()
                .map(l -> new TimeRange(l.getStartTime(), l.getEndTime()))
                .sorted(Comparator.comparing(r -> r.start))
                .collect(Collectors.toList());

        LocalTime current = start;
        for (TimeRange leave : sortedLeaves) {
            if (current.isBefore(leave.start)) {
                result.add(new TimeRange(current, leave.start));
            }
            if (leave.end.isAfter(current)) {
                current = leave.end;
            }
        }

        if (current.isBefore(end)) {
            result.add(new TimeRange(current, end));
        }

        return result;
    }

    private boolean isSegmentPunched(TimeRange segment, LocalTime inTime, LocalTime outTime) {
        if (inTime == null || outTime == null) {
            return false;
        }
        return !outTime.isBefore(segment.start) && !inTime.isAfter(segment.end);
    }

    public List<AttendanceRecord> getAttendanceByEmployeeAndMonth(Long employeeId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return dataStore.getAllAttendanceRecords().stream()
                .filter(a -> a.getEmployeeId().equals(employeeId))
                .filter(a -> !a.getDate().isBefore(start))
                .filter(a -> !a.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    private static class TimeRange {
        LocalTime start;
        LocalTime end;

        TimeRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
