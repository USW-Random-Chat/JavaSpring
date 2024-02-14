package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.emailAuth.repository.EmailTokenRepository;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.security.domain.Authority;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import com.USWRandomChat.backend.security.jwt.domain.Token;
import com.USWRandomChat.backend.security.jwt.repository.JwtRepository;
import com.USWRandomChat.backend.security.jwt.service.JwtService;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final EmailTokenRepository emailTokenRepository;
    private final JwtRepository jwtRepository;

    //회원가입
    public Member signUp(SignUpRequest request) {
        Member member = Member.builder().memberId(request.getMemberId())
                //password 암호화
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .build();

        member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
        memberRepository.save(member);

        //이메일 인증
        Member savedMemberEmail = memberRepository.findByEmail(member.getEmail());

        return savedMemberEmail;
    }

    //로그인
    public SignInResponse signIn(SignInRequest request) throws Exception {
        Member member = memberRepository.findByMemberId(request.getMemberId()).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정정보입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("잘못된 계정정보입니다.");
        }

        // refreshToken 생성은 Member 엔티티에 설정하지 않고, JwtService에서 처리
        String refreshToken = jwtService.createRefreshToken(member);
        log.info("memberId: {}, pw: {} - 로그인 완료", request.getMemberId(), request.getPassword());

        // SignInResponse 생성 시 refreshToken을 전달
        return new SignInResponse(member, jwtProvider, jwtService);
    }

    // 회원 탈퇴
    public void withdraw(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        // EMAIL_TOKEN 테이블과 관련된 데이터 삭제
        emailTokenRepository.deleteByMemberId(member.getId());

        // 저장된 Refresh Token을 찾아 삭제
        Optional<Token> refreshToken = jwtRepository.findById(member.getId());
        refreshToken.ifPresent(jwtRepository::delete);

        // 회원 삭제
        memberRepository.deleteById(member.getId());
        log.info("회원 탈퇴 완료: memberId={}", memberId);
    }

    //전체 조회
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    //id 조회
    public Member findById(Long id) {
        Optional<Member> byId = memberRepository.findById(id);
        if (byId.isPresent()) {
            //조회 성공
            return byId.get();
        } else {
            //조회 실패
            return byId.orElse(null);
        }
    }

    //중복 검증 memberId
    public boolean validateDuplicateMemberId(MemberDTO memberDTO) {
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        //중복
        //사용 가능한 ID
        return byMemberId.isPresent();
    }

    //중복 검증 nickname
    public boolean validateDuplicateMemberNickname(MemberDTO memberDTO) {
        Optional<Member> byNickname = memberRepository.findByNickname(memberDTO.getNickname());
        //중복
        //사용 가능한 ID
        return byNickname.isPresent();
    }

    //해당 토큰 유저 삭제
    public void deleteFromId(Long id) {
        memberRepository.deleteById(id);
    }
}