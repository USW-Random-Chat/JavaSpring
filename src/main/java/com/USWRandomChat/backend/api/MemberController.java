package com.USWRandomChat.backend.api;

import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.memberDTO.MemberDTO;
import com.USWRandomChat.backend.memberDTO.SignRequest;
import com.USWRandomChat.backend.memberDTO.SignResponse;
import com.USWRandomChat.backend.response.ListResponse;
import com.USWRandomChat.backend.response.ResponseService;
import com.USWRandomChat.backend.service.EmailService;
import com.USWRandomChat.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ResponseService responseService;
    private final EmailService emailService;

    //회원가입
    @PostMapping(value = "/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignRequest request) throws MessagingException {
        Member findMember = memberService.signup(request);
        return new ResponseEntity<>(emailService.createEmailToken(findMember), HttpStatus.OK);
    }

    //이메일 인증 확인
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        try {
            return new ResponseEntity<>(emailService.verifyEmail(uuid), HttpStatus.OK);
        } catch (Exception e) {
            log.error("DATABASE_ERROR - 토큰 에러: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

    //이메일 재인증
    @PostMapping("/reconfirm-email")
    public ResponseEntity<String> reconfirmEmail(@RequestParam String uuid) throws MessagingException {
        return new ResponseEntity<>(emailService.recreateEmailToken(uuid), HttpStatus.OK);
    }

    //로그인
    @PostMapping(value = "/sign-in")
    public ResponseEntity<SignResponse> signIn(@RequestBody SignRequest request) throws Exception {
        return new ResponseEntity<>(memberService.signIn(request), HttpStatus.OK);
    }

    //user인증 확인
    @GetMapping(value = "/user/get")
    public ResponseEntity<SignResponse> getUser(@RequestParam String memberId) throws Exception {
        return new ResponseEntity<>(memberService.getMember(memberId), HttpStatus.OK);
    }

    //admin인증 확인
    @PostMapping(value = "/admin/get")
    public ResponseEntity<SignResponse> getUserForAdmin(@RequestParam String memberId) throws Exception {
        return new ResponseEntity<>(memberService.getMember(memberId), HttpStatus.OK);
    }

    //전체 조회
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberService.findAll());
    }

    // memberID 중복 체크
    @PostMapping("/check-duplicate-id")
    public boolean idCheck(@RequestBody MemberDTO request) {
        boolean checkResult = memberService.validateDuplicateMemberId(request);
        if (checkResult == false) {
            //사용가능한 ID
            return true;
        } else {
            //중복
            return false;
        }
    }

    @PostMapping("/check-duplicate-nickname")
    public boolean NicknameCheck(@RequestBody MemberDTO request) {
        boolean checkResult = memberService.validateDuplicateMemberNickname(request);
        if (checkResult == false) {
            //사용가능한 Nickname
            return true;
        } else {
            //중복
            return false;
        }
    }

}