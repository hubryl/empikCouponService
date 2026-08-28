package com.empik.recruitment.couponservice.service;

import com.empik.recruitment.couponservice.dto.CouponDTO;
import com.empik.recruitment.couponservice.enums.CouponUseageEnum;

public interface CouponService {

    /**
     * Add new coupon
     * @param coupon {@link CouponDTO} to be added
     */
    void addCoupon(CouponDTO coupon);

    /**
     * Use coupon
     * @param couponCode - coupon code
     * @param userId - user for which coupon is used
     * @param ipAddress - IP address of user (used for country determination)
     * @return {@link CouponUseageEnum} with status of operation
     */
    CouponUseageEnum useCoupon(String couponCode, String userId, String ipAddress);

}
