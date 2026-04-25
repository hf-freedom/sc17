package com.attendance.service;

import com.attendance.entity.Shift;
import com.attendance.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    @Autowired
    private DataStore dataStore;

    public List<Shift> getAllShifts() {
        return dataStore.getAllShifts().stream()
                .filter(Shift::getActive)
                .collect(Collectors.toList());
    }

    public Shift getShiftById(Long id) {
        return dataStore.getShifts().get(id);
    }

    public Shift createShift(Shift shift) {
        shift.setId(dataStore.generateShiftId());
        if (shift.getActive() == null) {
            shift.setActive(true);
        }
        if (shift.getIsFlexible() == null) {
            shift.setIsFlexible(false);
        }
        if (shift.getLateGraceMinutes() == null) {
            shift.setLateGraceMinutes(10);
        }
        if (shift.getEarlyLeaveGraceMinutes() == null) {
            shift.setEarlyLeaveGraceMinutes(10);
        }
        dataStore.getShifts().put(shift.getId(), shift);
        return shift;
    }

    public Shift updateShift(Long id, Shift shift) {
        Shift existing = dataStore.getShifts().get(id);
        if (existing == null) {
            return null;
        }
        shift.setId(id);
        dataStore.getShifts().put(id, shift);
        return shift;
    }

    public boolean deleteShift(Long id) {
        Shift shift = dataStore.getShifts().get(id);
        if (shift == null) {
            return false;
        }
        shift.setActive(false);
        dataStore.getShifts().put(id, shift);
        return true;
    }
}
