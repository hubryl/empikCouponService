package com.empik.recruitment.couponservice.configuraiton;

import io.ipinfo.api.IPinfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouponServiceConfig {

    private final IpInfoProperties ipInfoProperties;

    public CouponServiceConfig(IpInfoProperties ipInfoProperties) {
        this.ipInfoProperties = ipInfoProperties;
    }

    @Bean
    public IPinfo ipInfo() {
        return new IPinfo.Builder()
                .setToken(ipInfoProperties.getToken())
                .build();
    }
}
