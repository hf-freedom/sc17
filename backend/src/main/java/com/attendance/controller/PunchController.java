package com.attendance.controller;

import com.attendance.entity.PunchRecord;
import com.attendance.service.PunchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/punch")
@CrossOrigin(origins = "*")
public class PunchController {

    @Autowired
    private PunchService punchService;

    @GetMapping
    public List<PunchRecord> getAllPunchRecords() {
        return punchService.getAllPunchRecords();
    }

    @GetMapping("/employee/{employeeId}")
    public List<PunchRecord> getPunchRecordsByEmployeeAndDate(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return punchService.getPunchRecordsByEmployeeAndDate(employeeId, date);
    }

    @PostMapping("/in")
    public PunchRecord clockIn(
            @RequestParam Long employeeId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String device) {
        return punchService.clockIn(employeeId, location, device);
    }

    @PostMapping("/out")
    public PunchRecord clockOut(
            @RequestParam Long employeeId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String device) {
        return punchService.clockOut(employeeId, location, device);
    }

    @GetMapping("/last/{employeeId}")
    public PunchRecord getLastPunch(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return punchService.getLastPunchByEmployeeAndDate(employeeId, date);
    }
}
