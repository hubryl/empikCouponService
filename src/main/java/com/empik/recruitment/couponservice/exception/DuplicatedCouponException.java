package com.empik.recruitment.couponservice.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;

public class DuplicatedCouponException extends DataIntegrityViolationException {

    /**
     * Constructor for DataIntegrityViolationException.
     *
     * @param msg the detail message
     */
    public DuplicatedCouponException(@Nullable String msg) {
        super(msg);
    }
}
