package com.project.www.service;

import com.project.www.enums.*;

import java.math.BigDecimal;

public interface EnrollmentService {
    /**
     * Processes a new student enrollment, handling referral validation, 
     * commission calculation, and wallet crediting.
     */
    void processEnrollment(Long studentId, Long courseId, Long batchId, BigDecimal amount, String referralCode);
}
