package com.empik.recruitment.couponservice.controller;

import com.empik.recruitment.couponservice.dto.CouponDTO;
import com.empik.recruitment.couponservice.dto.CouponUsageDTO;
import com.empik.recruitment.couponservice.enums.CouponUseageEnum;
import com.empik.recruitment.couponservice.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/coupon")
@Slf4j
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponDTO addCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        log.info("Creating coupon with code: {}", couponDTO.getCode());
        couponService.addCoupon(couponDTO);
        return couponDTO;
    }

    @PostMapping("/use")
    public ResponseEntity<CouponUseageEnum> useCoupon(@Valid @RequestBody CouponUsageDTO couponUsageDTO,
                                                      HttpServletRequest request) {
        log.info("Using coupon with code [{}] for user [{}]", couponUsageDTO.getCode(), couponUsageDTO.getUserId());
        final var result = couponService.useCoupon(
                couponUsageDTO.getCode(),
                couponUsageDTO.getUserId(),
                request.getRemoteAddr());
        return ResponseEntity.ok(result);
    }
}
