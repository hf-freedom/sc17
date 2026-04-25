package com.attendance.service;

import com.attendance.entity.OvertimeRequest;
import com.attendance.enums.ApprovalStatus;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OvertimeService {

    @Autowired
    private DataStore dataStore;

    public List<OvertimeRequest> getAllOvertimeRequests() {
        return dataStore.getAllOvertimeRequests();
    }

    public List<OvertimeRequest> getOvertimeRequestsByEmployee(Long employeeId) {
        return dataStore.getAllOvertimeRequests().stream()
                .filter(o -> o.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public OvertimeRequest getOvertimeRequestById(Long id) {
        return dataStore.getOvertimeRequests().get(id);
    }

    public OvertimeRequest createOvertimeRequest(OvertimeRequest overtimeRequest) {
        double hours = calculateHours(overtimeRequest.getStartTime(), overtimeRequest.getEndTime());
        overtimeRequest.setHours(hours);
        overtimeRequest.setId(dataStore.generateOvertimeRequestId());
        overtimeRequest.setStatus(ApprovalStatus.PENDING);
        overtimeRequest.setCreateTime(LocalDateTime.now());
        dataStore.getOvertimeRequests().put(overtimeRequest.getId(), overtimeRequest);
        return overtimeRequest;
    }

    public OvertimeRequest approveOvertime(Long id, String approver) {
        OvertimeRequest overtimeRequest = dataStore.getOvertimeRequests().get(id);
        if (overtimeRequest == null) {
            return null;
        }

        overtimeRequest.setStatus(ApprovalStatus.APPROVED);
        overtimeRequest.setApprover(approver);
        overtimeRequest.setApproveTime(LocalDateTime.now());
        dataStore.getOvertimeRequests().put(id, overtimeRequest);

        return overtimeRequest;
    }

    public OvertimeRequest rejectOvertime(Long id, String rejectReason) {
        OvertimeRequest overtimeRequest = dataStore.getOvertimeRequests().get(id);
        if (overtimeRequest == null) {
            return null;
        }

        overtimeRequest.setStatus(ApprovalStatus.REJECTED);
        overtimeRequest.setRejectReason(rejectReason);
        overtimeRequest.setApproveTime(LocalDateTime.now());
        dataStore.getOvertimeRequests().put(id, overtimeRequest);

        return overtimeRequest;
    }

    private double calculateHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        long minutes = ChronoUnit.MINUTES.between(start, end);
        return minutes / 60.0;
    }

    public double getTotalApprovedOvertimeHours(Long employeeId, int year, int month) {
        LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        return dataStore.getAllOvertimeRequests().stream()
                .filter(o -> o.getEmployeeId().equals(employeeId))
                .filter(o -> o.getStatus() == ApprovalStatus.APPROVED)
                .filter(o -> o.getEndTime().isAfter(monthStart))
                .filter(o -> o.getStartTime().isBefore(monthEnd))
                .mapToDouble(OvertimeRequest::getHours)
                .sum();
    }
}
