package com.empik.recruitment.couponservice.configuraiton;

import io.ipinfo.api.IPinfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouponServiceConfig {

    @Bean
    public IPinfo ipInfo() {
        return new IPinfo.Builder()
                .setToken("004e1cf888c8c6")
                .build();
    }
}
