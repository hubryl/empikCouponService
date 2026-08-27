package com.empik.recruitment.couponservice.repository;

import com.empik.recruitment.couponservice.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Integer> {

    boolean existsByCodeAndUserId(String code, String userId);

}
