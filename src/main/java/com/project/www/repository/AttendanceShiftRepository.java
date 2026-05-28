package com.project.www.repository;

import com.project.www.entity.AttendanceShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceShiftRepository extends JpaRepository<AttendanceShift, Long> {
    List<AttendanceShift> findAllByTenantId(Long tenantId);
    Optional<AttendanceShift> findByIdAndTenantId(Long id, Long tenantId);
    List<AttendanceShift> findAllByOfficeIdAndTenantId(Long officeId, Long tenantId);
}
