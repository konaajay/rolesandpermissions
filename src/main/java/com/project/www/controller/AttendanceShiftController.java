package com.project.www.controller;

import com.project.www.entity.AttendanceShift;
import com.project.www.entity.OfficeLocation;
import com.project.www.repository.AttendanceShiftRepository;
import com.project.www.repository.OfficeLocationRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class AttendanceShiftController {

    private final AttendanceShiftRepository attendanceShiftRepository;
    private final OfficeLocationRepository officeLocationRepository;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public List<AttendanceShift> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        return attendanceShiftRepository.findAllByTenantId(tenantId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public AttendanceShift create(@RequestBody AttendanceShift shift) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        if (shift.getOffice() == null || shift.getOffice().getId() == null) {
            throw new RuntimeException("Office location is required");
        }
        OfficeLocation office = officeLocationRepository.findByIdAndTenantId(shift.getOffice().getId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Office location not found under this tenant"));
        
        shift.setTenantId(tenantId);
        shift.setOffice(office);
        return attendanceShiftRepository.save(shift);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public AttendanceShift update(@PathVariable Long id, @RequestBody AttendanceShift details) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        AttendanceShift existing = attendanceShiftRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Attendance shift not found"));
        
        if (details.getOffice() == null || details.getOffice().getId() == null) {
            throw new RuntimeException("Office location is required");
        }
        OfficeLocation office = officeLocationRepository.findByIdAndTenantId(details.getOffice().getId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Office location not found under this tenant"));
        
        existing.setName(details.getName());
        existing.setStartTime(details.getStartTime());
        existing.setEndTime(details.getEndTime());
        existing.setGraceMinutes(details.getGraceMinutes());
        existing.setMinHalfDayMinutes(details.getMinHalfDayMinutes());
        existing.setMinFullDayMinutes(details.getMinFullDayMinutes());
        existing.setShortBreakStartTime(details.getShortBreakStartTime());
        existing.setShortBreakEndTime(details.getShortBreakEndTime());
        existing.setLongBreakStartTime(details.getLongBreakStartTime());
        existing.setLongBreakEndTime(details.getLongBreakEndTime());
        existing.setOffice(office);
        
        return attendanceShiftRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public void delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        AttendanceShift existing = attendanceShiftRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Attendance shift not found"));
        attendanceShiftRepository.delete(existing);
    }
}
