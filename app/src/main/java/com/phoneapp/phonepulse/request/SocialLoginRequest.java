package com.phoneapp.phonepulse.request;

public class SocialLoginRequest {
    private String provider;
    private String access_token;

    public SocialLoginRequest(String provider, String access_token) {
        this.provider = provider;
        this.access_token = access_token;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
