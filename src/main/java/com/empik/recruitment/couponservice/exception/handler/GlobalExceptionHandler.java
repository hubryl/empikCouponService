package com.empik.recruitment.couponservice.exception.handler;

import com.empik.recruitment.couponservice.exception.DuplicatedCouponException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatedCouponException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicate(DuplicatedCouponException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Coupon already exists");
    }

}
