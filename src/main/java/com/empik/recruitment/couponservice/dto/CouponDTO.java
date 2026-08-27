package com.empik.recruitment.couponservice.dto;

import com.empik.recruitment.couponservice.interfaces.LowerCase;
import com.empik.recruitment.couponservice.interfaces.UpperCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponDTO {

    @LowerCase
    @NotEmpty
    private String code;

    @Min(0)
    @Max(10000)
    private int maxUsages;

    @UpperCase
    @NotEmpty
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "Country must contain exactly two letters")
    private String country;
}
