package com.USWRandomChat.backend.global.security.jwt.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenResponse {
    // Getter와 Setter
    private String accessToken;
    private String refreshToken;

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
