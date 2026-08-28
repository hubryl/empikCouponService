package com.empik.recruitment.couponservice.dto;

import com.empik.recruitment.couponservice.interfaces.LowerCase;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponUsageDTO {

    @LowerCase
    @NotEmpty
    private String code;

    @LowerCase
    @NotEmpty
    private String userId;
}
