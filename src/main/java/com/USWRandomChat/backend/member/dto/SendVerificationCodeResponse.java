package com.USWRandomChat.backend.member.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SendVerificationCodeResponse {

    private String uuid;

    public SendVerificationCodeResponse(String uuid) {
        this.uuid = uuid;
    }
}