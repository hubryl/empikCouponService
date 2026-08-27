package com.empik.recruitment.couponservice.enums;

public enum CouponUseageEnum {

    SUCCESS("Success"),
    COUPON_NOT_VALID("Invalid Coupon"),
    ALREADY_USED("Coupon already used"),
    COUNTRY_DETERMINATION_FAILED("country determination failed");

    private final String couponUseage;

    CouponUseageEnum(String couponUseage) {
        this.couponUseage = couponUseage;
    }

    @Override
    public String toString() {
        return couponUseage;
    }

}
