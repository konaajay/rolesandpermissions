package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.CouponCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CouponCourseRepository extends JpaRepository<CouponCourse, Long> {
    List<CouponCourse> findByCoupon_Id(Long couponId);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByCoupon_Id(Long couponId);
}
