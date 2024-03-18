package com.USWRandomChat.backend.member.api;

import com.USWRandomChat.backend.emailAuth.service.EmailService;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.global.response.ListResponse;
import com.USWRandomChat.backend.global.response.ResponseService;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenResponse;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.service.FindIdService;
import com.USWRandomChat.backend.member.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
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
    private final JwtService jwtService;
    private final FindIdService findIdService;

    //임시 회원가입
    @PostMapping(value = "/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) throws MessagingException {
        MemberTemp findTempMember = memberService.signUpMemberTemp(request);
        return new ResponseEntity<>(new SignUpResponse(emailService.createEmailToken(findTempMember))
                , HttpStatus.OK);
    }

    //이메일 인증 확인 후 회원가입
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        MemberTemp memberTemp = emailService.findByUuid(uuid);
        memberService.signUpMember(memberTemp);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping(value = "/sign-up-finish")
    public ResponseEntity<Boolean> signUpFinish(@RequestParam String uuid) {
        return new ResponseEntity<>(memberService.signUpFinish(uuid), HttpStatus.OK);
    }

    //로그인
    @PostMapping(value = "/sign-in")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest request) {
        return new ResponseEntity<>(memberService.signIn(request), HttpStatus.OK);
    }

    //자동 로그인 로직: 엑세스 토큰, 리프레시 토큰 재발급
    @PostMapping("/auto-sign-in")
    public ResponseEntity<ApiResponse> autoSignIn(@RequestHeader("Authorization") String accessToken){
        try {
            String newAccessToken = jwtService.refreshAccessToken(accessToken);
            TokenResponse response = new TokenResponse(newAccessToken);
            return ResponseEntity.ok(new ApiResponse("자동로그인이 성공했습니다.", response));
        } catch (Exception  e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("자동로그인에 실패했습니다."));
        }
    }

    //로그아웃
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponse> signOut(@RequestHeader("Authorization") String accessToken){
        try {
            jwtService.signOut(accessToken);
            return ResponseEntity.ok(new ApiResponse("로그아웃 되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("로그아웃에 실패했습니다."));
        }
    }

    //회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@RequestHeader("Authorization") String accessToken) {
        try {
            memberService.withdraw(accessToken);
            return ResponseEntity.ok(new ApiResponse("회원 탈퇴가 완료됐습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("회원 탈퇴에 실패했습니다."));
        }
    }

    //이메일 재인증
    @PostMapping("/reconfirm-email")
    public ResponseEntity<ApiResponse> reconfirmEmail(@Valid @RequestParam String uuid) {
        try{
            emailService.recreateEmailToken(uuid);
            return ResponseEntity.ok(new ApiResponse("이메일 재인증을 해주세요", uuid));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("이메일 재인증에 실패했습니다."));
        }
    }

    //전체 조회
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberService.findAll());
    }

    //계정 중복 체크
    @PostMapping("/check-duplicate-account")
    public ResponseEntity<ApiResponse> checkDuplicateAccount(@RequestBody MemberDTO request) {
        try {
            memberService.checkDuplicateAccount(request);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 계정입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("이미 사용하고 있는 계정입니다."));
        }
    }

    //이메일 중복 확인
    @PostMapping("/check-duplicate-email")
    public ResponseEntity<ApiResponse> checkDuplicateEmail(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.checkDuplicateEmail(memberDTO);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 이메일입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("이미 사용하고 있는 이메일입니다."));
        }
    }

    //회원가입 시의 닉네임 확인
    @PostMapping("/check-duplicate-nickname-signUp")
    public ResponseEntity<ApiResponse> checkDuplicateNicknameSignUp(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.checkDuplicateNicknameSignUp(memberDTO);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 닉네임입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("이미 사용하고 있는 닉네임입니다."));
        }
    }

    //이미 가입된 사용자의 닉네임 중복 확인
    @PostMapping("/check-nickname")
    public ResponseEntity<ApiResponse> checkDuplicateNickname(@RequestHeader("Authorization") String accessToken, @RequestBody MemberDTO memberDTO) {
        try {
            memberService.checkDuplicateNickname(accessToken, memberDTO);
            return ResponseEntity.ok(new ApiResponse( "닉네임 사용 가능합니다."));
        } catch (AccountException e) {
            if (e.getExceptionType() == ExceptionType.NICKNAME_EXPIRATION_TIME) {
                // 닉네임 변경 30일 제한 예외 처리
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse("닉네임을 변경한 지 30일이 지나지 않았습니다."));
            } else {
                // 닉네임 중복 예외 처리
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse( "이미 사용하고 있는 닉네임입니다."));
            }
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.internalServerError().body(new ApiResponse("서버 오류가 발생했습니다."));
        }
    }

    //Id 찾기 로직: 이메일 인증된 회원만
    @PostMapping(value = "/find-Id")
    public ResponseEntity<Boolean> findId(@RequestParam String email) throws MessagingException {
        return new ResponseEntity<>(findIdService.findById(email), HttpStatus.OK);
    }

    @Data
    @AllArgsConstructor
    static class SignUpResponse {

        private String uuid;
    }
}