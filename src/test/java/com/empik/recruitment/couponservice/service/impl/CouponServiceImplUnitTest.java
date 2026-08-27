package com.empik.recruitment.couponservice.service.impl;

import com.empik.recruitment.couponservice.dto.CouponDTO;
import com.empik.recruitment.couponservice.entity.Coupon;
import com.empik.recruitment.couponservice.entity.CouponUsage;
import com.empik.recruitment.couponservice.enums.CouponUseageEnum;
import com.empik.recruitment.couponservice.repository.CouponRepository;
import com.empik.recruitment.couponservice.repository.CouponUsageRepository;
import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CouponServiceImplUnitTest {
    private static final String COUPON_CODE = "SUMMER2026";
    private static final String USER_ID = "user123";
    private static final String IP_ADDRESS = "8.8.8.8";
    private static final String COUNTRY = "PL";

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponUsageRepository couponUsageRepository;
    @Mock
    private IPinfo iPinfo;

    private CouponServiceImpl tested;

    @BeforeEach
    void setUp() {
        tested = new CouponServiceImpl(couponRepository, couponUsageRepository, iPinfo);
    }

    // -------------------------------------------------------------------------
    // addCoupon()
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateNewCouponWhenCouponDoesNotExist() {

        // Given
        CouponDTO couponDTO = createCouponDTO();
        when(couponRepository.findById(COUPON_CODE))
                .thenReturn(Optional.empty());

        // When
        tested.addCoupon(couponDTO);

        // Then
        ArgumentCaptor<Coupon> couponCaptor =
                ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).save(couponCaptor.capture());
        Coupon savedCoupon = couponCaptor.getValue();
        assertEquals(COUPON_CODE, savedCoupon.getCode());
        assertEquals(10, savedCoupon.getMaxUsages());
        assertEquals(COUNTRY, savedCoupon.getCountry());
        assertEquals(0, savedCoupon.getUsages());
        assertNotNull(savedCoupon.getCreatedAt());
    }

    @Test
    void shouldUpdateExistingCoupon() {

        // Given
        CouponDTO couponDTO = createCouponDTO();
        Coupon existingCoupon = new Coupon();
        existingCoupon.setCode(COUPON_CODE);
        existingCoupon.setMaxUsages(5);
        existingCoupon.setCountry("DE");
        existingCoupon.setUsages(2);
        when(couponRepository.findById(COUPON_CODE))
                .thenReturn(Optional.of(existingCoupon));

        // When
        tested.addCoupon(couponDTO);

        // Then
        assertEquals(10, existingCoupon.getMaxUsages());
        assertEquals(COUNTRY, existingCoupon.getCountry());
        assertEquals(2, existingCoupon.getUsages());
        verify(couponRepository).save(existingCoupon);
    }

    // -------------------------------------------------------------------------
    // useCoupon()
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnNotValidWhenCouponDoesNotExist() {

        // Given
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.empty());

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);

        verifyNoInteractions(couponUsageRepository);
        verifyNoInteractions(iPinfo);
    }

    @Test
    void shouldReturnNotValidWhenCouponUsageLimitHasBeenReached() {

        // Given
        Coupon coupon = createCoupon(10, 10);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);
        verifyNoInteractions(couponUsageRepository);
        verifyNoInteractions(iPinfo);
    }

    @Test
    void shouldReturnNotValidWhenCountryDoesNotMatchIpCountry() throws Exception {

        // Given
        Coupon coupon = createCoupon(10, 1);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));
        var ipInfoResponse = mock(io.ipinfo.api.model.IPResponse.class);
        when(iPinfo.lookupIP(IP_ADDRESS)).thenReturn(ipInfoResponse);
        when(ipInfoResponse.getCountryCode()).thenReturn("DE");

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);
        verify(couponRepository, never()).tryReserveUsage(any());
        verifyNoInteractions(couponUsageRepository);
    }

    @Test
    void shouldReturnAlreadyUsedWhenUserAlreadyUsedCoupon() throws Exception {
        // Given
        Coupon coupon = createCoupon(10, 1);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));
        var ipInfoResponse = mock(io.ipinfo.api.model.IPResponse.class);
        when(iPinfo.lookupIP(IP_ADDRESS)).thenReturn(ipInfoResponse);
        when(ipInfoResponse.getCountryCode()).thenReturn(COUNTRY);
        when(couponUsageRepository.existsByCodeAndUserId(COUPON_CODE, USER_ID)).thenReturn(true);

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.ALREADY_USED, result);
        verify(couponRepository, never()).tryReserveUsage(any());
    }

    @Test
    void shouldReturnNotValidWhenUsageReservationFails() throws Exception {
        // Given
        Coupon coupon = createCoupon(10, 1);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));
        var ipInfoResponse = mock(io.ipinfo.api.model.IPResponse.class);
        when(iPinfo.lookupIP(IP_ADDRESS)).thenReturn(ipInfoResponse);
        when(ipInfoResponse.getCountryCode()).thenReturn(COUNTRY);
        when(couponUsageRepository.existsByCodeAndUserId(COUPON_CODE, USER_ID)).thenReturn(false);
        when(couponRepository.tryReserveUsage(COUPON_CODE)).thenReturn(0);

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);
        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void shouldSuccessfullyUseCoupon() throws Exception {
        // Given
        Coupon coupon = createCoupon(10, 1);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));
        var ipInfoResponse = mock(io.ipinfo.api.model.IPResponse.class);
        when(iPinfo.lookupIP(IP_ADDRESS)).thenReturn(ipInfoResponse);
        when(ipInfoResponse.getCountryCode()).thenReturn(COUNTRY);
        when(couponUsageRepository.existsByCodeAndUserId(COUPON_CODE, USER_ID)).thenReturn(false);
        when(couponRepository.tryReserveUsage(COUPON_CODE)).thenReturn(1);

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.SUCCESS, result);
        ArgumentCaptor<CouponUsage> usageCaptor = ArgumentCaptor.forClass(CouponUsage.class);
        verify(couponUsageRepository).save(usageCaptor.capture());
        CouponUsage savedUsage = usageCaptor.getValue();
        assertEquals(COUPON_CODE, savedUsage.getCode());
        assertEquals(USER_ID, savedUsage.getUserId());
        verify(couponRepository).tryReserveUsage(COUPON_CODE);
        verify(couponRepository, never()).tryReleaseUsage(any());
    }

    @Test
    void shouldReleaseUsageAndReturnNotValidWhenSavingUsageFails() throws Exception {

        // Given
        Coupon coupon = createCoupon(10, 1);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));
        var ipInfoResponse = mock(io.ipinfo.api.model.IPResponse.class);
        when(iPinfo.lookupIP(IP_ADDRESS)).thenReturn(ipInfoResponse);
        when(ipInfoResponse.getCountryCode()).thenReturn(COUNTRY);
        when(couponUsageRepository.existsByCodeAndUserId(COUPON_CODE, USER_ID)).thenReturn(false);
        when(couponRepository.tryReserveUsage(COUPON_CODE)).thenReturn(1);
        when(couponUsageRepository.save(any(CouponUsage.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(couponRepository.tryReleaseUsage(COUPON_CODE)).thenReturn(1);

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);
        verify(couponRepository).tryReserveUsage(COUPON_CODE);
        verify(couponRepository).tryReleaseUsage(COUPON_CODE);
    }

    @Test
    void shouldReturnNotValidWhenReleaseUsageFails() throws Exception {

        // Given
        Coupon coupon = createCoupon(10, 1);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));
        var ipInfoResponse = mock(io.ipinfo.api.model.IPResponse.class);
        when(iPinfo.lookupIP(IP_ADDRESS)).thenReturn(ipInfoResponse);
        when(ipInfoResponse.getCountryCode()).thenReturn(COUNTRY);
        when(couponUsageRepository.existsByCodeAndUserId(COUPON_CODE, USER_ID)).thenReturn(false);
        when(couponRepository.tryReserveUsage(COUPON_CODE)).thenReturn(1);
        when(couponUsageRepository.save(any(CouponUsage.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(couponRepository.tryReleaseUsage(COUPON_CODE)).thenReturn(0);

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);
        verify(couponRepository).tryReleaseUsage(COUPON_CODE);
    }

    // -------------------------------------------------------------------------
    // IPinfo error handling
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnNotValidWhenIpinfoIsRateLimited() throws Exception {

        // Given
        Coupon coupon = createCoupon(10, 1);

        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));

        when(iPinfo.lookupIP(IP_ADDRESS)).thenThrow(mock(RateLimitedException.class));

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);
        verify(couponRepository, never()).tryReserveUsage(any());
        verifyNoInteractions(couponUsageRepository);
    }

    @Test
    void shouldReturnNotValidWhenIpinfoThrowsException() throws Exception {

        // Given
        Coupon coupon = createCoupon(100, 1);
        when(couponRepository.findById(COUPON_CODE)).thenReturn(Optional.of(coupon));
        when(iPinfo.lookupIP(IP_ADDRESS)).thenThrow(new RuntimeException("IPinfo unavailable"));

        // When
        CouponUseageEnum result = tested.useCoupon(COUPON_CODE, USER_ID, IP_ADDRESS);

        // Then
        assertEquals(CouponUseageEnum.COUPON_NOT_VALID, result);
        verify(couponRepository, never()).tryReserveUsage(any());
        verifyNoInteractions(couponUsageRepository);
    }

    private CouponDTO createCouponDTO() {
        CouponDTO dto = new CouponDTO();
        dto.setCode(COUPON_CODE);
        dto.setMaxUsages(10);
        dto.setCountry(COUNTRY);
        return dto;
    }

    private Coupon createCoupon(int maxUsages, int usages) {
        Coupon coupon = new Coupon();
        coupon.setCode(COUPON_CODE);
        coupon.setMaxUsages(maxUsages);
        coupon.setUsages(usages);
        coupon.setCountry(COUNTRY);
        return coupon;
    }
}
