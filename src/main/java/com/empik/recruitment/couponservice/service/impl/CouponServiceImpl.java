package com.empik.recruitment.couponservice.service.impl;

import com.empik.recruitment.couponservice.configuraiton.IpInfoProperties;
import com.empik.recruitment.couponservice.dto.CouponDTO;
import com.empik.recruitment.couponservice.entity.Coupon;
import com.empik.recruitment.couponservice.entity.CouponUsage;
import com.empik.recruitment.couponservice.enums.CouponUseageEnum;
import com.empik.recruitment.couponservice.exception.DuplicatedCouponException;
import com.empik.recruitment.couponservice.repository.CouponRepository;
import com.empik.recruitment.couponservice.repository.CouponUsageRepository;
import com.empik.recruitment.couponservice.service.CouponService;
import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final IPinfo iPinfo;
    private final IpInfoProperties ipInfoProperties;

    public CouponServiceImpl(CouponRepository couponRepository,
                             CouponUsageRepository couponUsageRepository,
                             IPinfo iPinfo,
                             IpInfoProperties ipInfoProperties) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.iPinfo = iPinfo;
        this.ipInfoProperties = ipInfoProperties;
    }

    @Override
    public void addCoupon(CouponDTO couponDTO) {
        final var coupon = couponRepository.findById(couponDTO.getCode());
        if (coupon.isPresent()) {
            throw new DuplicatedCouponException("Coupon already exists");
        }
        final var newCoupon = createCouponFromDto(couponDTO);
        couponRepository.save(newCoupon);
    }

    private Coupon createCouponFromDto(CouponDTO couponDto) {
        final var coupon = new Coupon();
        BeanUtils.copyProperties(couponDto, coupon);
        coupon.setCreatedAt(new Date());
        coupon.setUsages(0);
        return coupon;
    }

    @Override
    @Transactional
    public CouponUseageEnum useCoupon(String couponCode, String userId, String ipAddress) {
        final CouponUseageEnum couponNotValid = canUseCoupon(couponCode, userId, ipAddress);
        if (couponNotValid != null) { return couponNotValid; }
        CouponUsage couponUsage = new CouponUsage();
        couponUsage.setCode(couponCode);
        couponUsage.setUserId(userId);
        try {
            couponUsageRepository.save(couponUsage);
        } catch (DataIntegrityViolationException e) {
            if (couponRepository.tryReleaseUsage(couponCode) == 0) {
                log.warn("WARNC0001: Coupon {} has not been released", couponCode);
            }
            return CouponUseageEnum.COUPON_NOT_VALID;
        }
        return CouponUseageEnum.SUCCESS;
    }

    private @Nullable CouponUseageEnum canUseCoupon(String couponCode, String userId, String ipAddress) {
        final var couponOpt = couponRepository.findById(couponCode);
        if (couponOpt.isEmpty()) {
            return CouponUseageEnum.COUPON_NOT_VALID;
        }
        if (couponOpt.get().getUsages() >= couponOpt.get().getMaxUsages()) {
            return CouponUseageEnum.COUPON_NOT_VALID;
        }
        if (!isValidCountryCode(couponOpt.get().getCountry(), ipAddress)) {
            return CouponUseageEnum.COUPON_NOT_VALID;
        }
        if (couponUsageRepository.existsByCodeAndUserId(couponCode, userId)) {
            return CouponUseageEnum.ALREADY_USED;
        }
        if (couponRepository.tryReserveUsage(couponCode) == 0) {
            return CouponUseageEnum.ALREADY_USED;
        }
        return null;
    }

    private boolean isValidCountryCode(String country, String ipAddress) {
        if (ipInfoProperties.isDisabled()) {
            return true;
        }
        try {
            final var countryCode = iPinfo.lookupIP(ipAddress).getCountryCode();
            return Strings.CI.compare(country, countryCode) == 0;
        } catch (RateLimitedException e) {
            log.error("ERRIP0003: Cant do more IP checkups! Renew account", e);
            return false;
        } catch (Exception e) {
            log.error("ERRIP0002: Problem with IP checkup!", e);
            return false;
        }
    }
}
