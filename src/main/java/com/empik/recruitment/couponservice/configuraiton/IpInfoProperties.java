package com.empik.recruitment.couponservice.configuraiton;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ipinfo.lookupip")
public class IpInfoProperties {

    private boolean disabled;
    private String token;

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
