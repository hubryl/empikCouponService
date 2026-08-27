package com.empik.recruitment.couponservice.repository;

import com.empik.recruitment.couponservice.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {

    /**
     * Atomic operation for reserving a coupon
     *
     * @param code - coupon code
     * @return the number of rows updated (1 if reserved successfully, 0 if the
     * *         coupon does not exist or {@code usages >= maxUsages})
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Coupon c SET c.usages = c.usages + 1 " +
           "WHERE c.code = :code AND c.usages < c.maxUsages")
    int tryReserveUsage(@Param("code") String code);

    /**
     * Atomic operation for releasing reservation of a coupon
     *
     * @param code - coupon code
     * @return the number of rows updated (1 if released successfully, 0 if the
     * *         reservation does not exist or {@code usages < 1 })
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Coupon c SET c.usages = c.usages - 1 " +
           "WHERE c.code = :code AND c.usages > 0")
    int tryReleaseUsage(@Param("code") String code);

}
