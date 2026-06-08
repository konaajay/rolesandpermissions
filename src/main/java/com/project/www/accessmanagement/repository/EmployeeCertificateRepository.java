package com.project.www.accessmanagement.repository;

import com.project.www.accessmanagement.entity.EmployeeCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeCertificateRepository extends JpaRepository<EmployeeCertificate, Long> {
    List<EmployeeCertificate> findByTenantId(Long tenantId);
    Optional<EmployeeCertificate> findByVerificationToken(String token);
    Optional<EmployeeCertificate> findByCertificateNo(String certificateNo);
    Optional<EmployeeCertificate> findByTenantIdAndCertificateNo(Long tenantId, String certificateNo);
}
