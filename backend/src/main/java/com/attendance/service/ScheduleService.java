package com.attendance.service;

import com.attendance.entity.Schedule;
import com.attendance.entity.Shift;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private AttendanceService attendanceService;

    public List<Schedule> getAllSchedules() {
        return dataStore.getAllSchedules();
    }

    public List<Schedule> getSchedulesByDate(LocalDate date) {
        return dataStore.getAllSchedules().stream()
                .filter(s -> s.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Schedule> getSchedulesByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return dataStore.getAllSchedules().stream()
                .filter(s -> s.getEmployeeId().equals(employeeId))
                .filter(s -> !s.getDate().isBefore(startDate) && !s.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public Schedule getScheduleByEmployeeAndDate(Long employeeId, LocalDate date) {
        return dataStore.getAllSchedules().stream()
                .filter(s -> s.getEmployeeId().equals(employeeId))
                .filter(s -> s.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    public Schedule createSchedule(Schedule schedule) {
        Schedule existing = getScheduleByEmployeeAndDate(schedule.getEmployeeId(), schedule.getDate());
        if (existing != null) {
            return updateSchedule(existing.getId(), schedule);
        }
        schedule.setId(dataStore.generateScheduleId());
        dataStore.getSchedules().put(schedule.getId(), schedule);
        attendanceService.recalculateAttendance(schedule.getEmployeeId(), schedule.getDate());
        return schedule;
    }

    public Schedule updateSchedule(Long id, Schedule schedule) {
        Schedule existing = dataStore.getSchedules().get(id);
        if (existing == null) {
            return null;
        }
        Long employeeId = existing.getEmployeeId();
        LocalDate date = existing.getDate();
        schedule.setId(id);
        dataStore.getSchedules().put(id, schedule);
        attendanceService.recalculateAttendance(employeeId, date);
        return schedule;
    }

    public boolean deleteSchedule(Long id) {
        Schedule schedule = dataStore.getSchedules().remove(id);
        if (schedule == null) {
            return false;
        }
        attendanceService.recalculateAttendance(schedule.getEmployeeId(), schedule.getDate());
        return true;
    }

    public void createBatchSchedules(List<Schedule> schedules) {
        for (Schedule schedule : schedules) {
            createSchedule(schedule);
        }
    }
}
