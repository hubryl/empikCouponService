package com.empik.recruitment.couponservice.controller;

import com.empik.recruitment.couponservice.dto.CouponDTO;
import com.empik.recruitment.couponservice.dto.CouponUsageDTO;
import com.empik.recruitment.couponservice.enums.CouponUseageEnum;
import com.empik.recruitment.couponservice.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
    @Operation(operationId = "addCoupon", summary = "Adding new Coupon", description = "Creates coupon for given market")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    name = "sample",
                    value = "{\"code\":\"ZIMA2016\",\"userId\":\"john.doe@google.pl\"}"
            )
    ))
    public CouponDTO addCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        log.debug("Creating coupon with code: {}", couponDTO.getCode());
        couponService.addCoupon(couponDTO);
        return couponDTO;
    }

    @PostMapping("/use")
    @Operation(operationId = "useCoupon", summary = "Use Coupon", description = "Creates usage of a coupon for given user only if country calculated from IP matches and coupon is active")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                    name = "sample",
                    value = "{\"code\":\"ZIMA2016\",\"maxUsages\":30,\"country\":\"PL\"}"
            )
    ))
    public ResponseEntity<CouponUseageEnum> useCoupon(@Valid @RequestBody CouponUsageDTO couponUsageDTO,
                                                      HttpServletRequest request) {
        log.debug("Using coupon with code [{}] for user [{}] ip: [{}]", couponUsageDTO.getCode(), couponUsageDTO.getUserId(), request.getRemoteAddr());
        final var result = couponService.useCoupon(
                couponUsageDTO.getCode(),
                couponUsageDTO.getUserId(),
                request.getRemoteAddr());
        return ResponseEntity.ok(result);
    }
}
