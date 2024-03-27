package com.USWRandomChat.backend.member.open.api;

import com.USWRandomChat.backend.member.dto.SendVerificationCodeRequest;
import com.USWRandomChat.backend.member.dto.SendVerificationCodeResponse;
import com.USWRandomChat.backend.member.dto.UpdatePasswordRequest;
import com.USWRandomChat.backend.member.open.service.PasswordUpdateService;
import com.USWRandomChat.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordUpdateController {

    private final PasswordUpdateService passwordUpdateService;

    //인증번호 생성 및 전송 요청 처리
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        String uuid = String.valueOf(passwordUpdateService.sendVerificationCode(request.getAccount(), request.getEmail()));
        SendVerificationCodeResponse response = new SendVerificationCodeResponse(uuid);
        return ResponseEntity.ok(new ApiResponse("인증번호가 전송되었습니다.", response));
    }

    //인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestParam String uuid, @RequestParam String verificationCode) {
        boolean isVerified = passwordUpdateService.verifyCode(uuid, verificationCode);
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse("인증번호가 확인됐습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("인증번호가 맞지 않습니다."));
        }
    }

    //비밀번호 변경 요청 처리
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestParam String uuid, @RequestBody UpdatePasswordRequest request) {
        passwordUpdateService.updatePassword(uuid, request.getNewPassword(), request.getConfirmNewPassword());
        return ResponseEntity.ok(new ApiResponse("비밀번호가 변경됐습니다."));
    }
}