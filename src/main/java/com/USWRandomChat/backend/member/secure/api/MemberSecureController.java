package com.USWRandomChat.backend.member.secure.api;

import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.member.dto.MemberDto;
import com.USWRandomChat.backend.member.secure.service.MemberSecureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/secure/member")
@RequiredArgsConstructor
public class MemberSecureController {

    private final MemberSecureService memberSecureService;

    //로그아웃
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponse> signOut(HttpServletRequest request, HttpServletResponse response) {
        memberSecureService.signOut(request, response);
        return ResponseEntity.ok(new ApiResponse("로그아웃 되었습니다."));
    }

    //회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(HttpServletRequest request) {
        memberSecureService.withdraw(request);
        return ResponseEntity.ok(new ApiResponse("회원 탈퇴가 완료됐습니다."));
    }
}