package com.attendance.service;

import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.enums.ApprovalStatus;
import com.attendance.enums.LeaveType;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceService attendanceService;

    public List<LeaveRequest> getAllLeaveRequests() {
        return dataStore.getAllLeaveRequests();
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return dataStore.getAllLeaveRequests().stream()
                .filter(l -> l.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public LeaveRequest getLeaveRequestById(Long id) {
        return dataStore.getLeaveRequests().get(id);
    }

    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveType() == LeaveType.ANNUAL) {
            Employee employee = employeeService.getEmployeeById(leaveRequest.getEmployeeId());
            if (employee == null) {
                throw new RuntimeException("员工不存在");
            }
            long leaveDays = ChronoUnit.DAYS.between(
                    leaveRequest.getStartTime().toLocalDate(),
                    leaveRequest.getEndTime().toLocalDate().plusDays(1)
            );
            if (employee.getAnnualLeaveBalance() < leaveDays) {
                throw new RuntimeException("年假余额不足");
            }
        }

        leaveRequest.setId(dataStore.generateLeaveRequestId());
        leaveRequest.setStatus(ApprovalStatus.PENDING);
        leaveRequest.setCreateTime(LocalDateTime.now());
        dataStore.getLeaveRequests().put(leaveRequest.getId(), leaveRequest);
        return leaveRequest;
    }

    public LeaveRequest approveLeave(Long id, String approver) {
        LeaveRequest leaveRequest = dataStore.getLeaveRequests().get(id);
        if (leaveRequest == null) {
            return null;
        }

        if (leaveRequest.getLeaveType() == LeaveType.ANNUAL) {
            long leaveDays = ChronoUnit.DAYS.between(
                    leaveRequest.getStartTime().toLocalDate(),
                    leaveRequest.getEndTime().toLocalDate().plusDays(1)
            );
            Employee employee = employeeService.updateAnnualLeaveBalance(
                    leaveRequest.getEmployeeId(),
                    -(int) leaveDays
            );
            if (employee == null) {
                throw new RuntimeException("年假余额不足");
            }
        }

        leaveRequest.setStatus(ApprovalStatus.APPROVED);
        leaveRequest.setApprover(approver);
        leaveRequest.setApproveTime(LocalDateTime.now());
        dataStore.getLeaveRequests().put(id, leaveRequest);

        recalculateAttendanceForLeave(leaveRequest);

        return leaveRequest;
    }

    public LeaveRequest rejectLeave(Long id, String rejectReason) {
        LeaveRequest leaveRequest = dataStore.getLeaveRequests().get(id);
        if (leaveRequest == null) {
            return null;
        }

        leaveRequest.setStatus(ApprovalStatus.REJECTED);
        leaveRequest.setRejectReason(rejectReason);
        leaveRequest.setApproveTime(LocalDateTime.now());
        dataStore.getLeaveRequests().put(id, leaveRequest);

        return leaveRequest;
    }

    private void recalculateAttendanceForLeave(LeaveRequest leaveRequest) {
        LocalDate startDate = leaveRequest.getStartTime().toLocalDate();
        LocalDate endDate = leaveRequest.getEndTime().toLocalDate();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            attendanceService.recalculateAttendance(leaveRequest.getEmployeeId(), current);
            current = current.plusDays(1);
        }
    }
}
