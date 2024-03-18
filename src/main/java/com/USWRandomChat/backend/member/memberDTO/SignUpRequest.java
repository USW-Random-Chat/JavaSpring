package com.USWRandomChat.backend.member.memberDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

    private String account;
    private String password;
    private String email;
    private String nickname;
    private Boolean isEmailVerified = false;
}
