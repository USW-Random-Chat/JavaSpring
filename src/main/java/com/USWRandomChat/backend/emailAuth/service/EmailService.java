package com.USWRandomChat.backend.emailAuth.service;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.emailAuth.repository.EmailTokenRepository;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.member.repository.MemberTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final MemberTempRepository memberTempRepository;

    //서버주소
    private static final String BASE_URL = "http://localhost:8080";
    //이메일 인증 경로
    private static final String CONFIRM_EMAIL_PATH = "/member/confirm-email";

    // 이메일 토큰 만료 시간
    private static final long EMAIL_TOKEN_EXPIRATION_TIME_VALUE = 5L;

    @Async
    public void sendEmail(MimeMessage mimeMessage) {
        javaMailSender.send(mimeMessage);
    }

    @Transactional
    public boolean verifyEmail(String uuid) {
        //이메일 토큰을 찾아옴 ( 만료되지 않고, 현재보다 이후에 만료되는 토큰이어야함)
        EmailToken findEmailToken = findByUuidAndExpirationDateAfterAndExpired(uuid);

        //토큰의 유저 ID를 이용하여 유저 인증 정보를 가져온다.
        MemberTemp findMember = findEmailToken.getMemberTemp();

        // 사용 완료
        findEmailToken.setTokenToUsed();
        deleteEmailTokenByUuid(uuid);

        // 회원 이메일 인증 여부 true로 바꾼다.
        findMember.setVerified();

        return true;
    }

    // 이메일 인증 토큰 생성
    public String createEmailToken(MemberTemp memberTemp) throws MessagingException {

        // 이메일 토큰 저장
        EmailToken emailToken = EmailToken.createEmailToken(memberTemp);
        emailTokenRepository.save(emailToken);

        // 이메일 전송을 위한 MimeMessage 생성 및 설정
        MimeMessage mimeMessage = createVerifyLink(memberTemp, emailToken);

        // 이메일 전송
        sendEmail(mimeMessage);

        return emailToken.getUuid();
    }

    // 이메일 인증 토큰 업데이트
    @Transactional
    public EmailToken updateEmailToken(String uuid) {

        //uuid로 토큰 찾기
        EmailToken findToken = emailTokenRepository.findByUuid(uuid).get();

        //uuid의 소유자 찾기
        MemberTemp findMember = memberTempRepository.findById(findToken.getId()).get();

        findToken.updateExpiredToken(LocalDateTime.now().plusMinutes(EMAIL_TOKEN_EXPIRATION_TIME_VALUE),
                false,
                findMember);
        emailTokenRepository.save(findToken);

        return findToken;
    }

    // 이메일 재인증 토큰 생성
    public String recreateEmailToken(String uuid) throws MessagingException {

        //회원의 uuid업데이트
        EmailToken updateEmailToken = updateEmailToken(uuid);

        // 이메일 전송을 위한 MimeMessage 생성 및 설정
        MimeMessage mimeMessage = createVerifyLink(updateEmailToken.getMemberTemp(), updateEmailToken);

        // 이메일 전송
        sendEmail(mimeMessage);

        // 인증메일 전송 시 토큰 반환
        return updateEmailToken.getUuid();

    }

    //이메일 인증 링크 생성
    public MimeMessage createVerifyLink(MemberTemp memberTemp, EmailToken emailToken) throws MessagingException {
        // 이메일 전송을 위한 MimeMessage 생성 및 설정
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(memberTemp.getEmail()+"@suwon.ac.kr");
        helper.setSubject("회원가입 이메일 인증");
        helper.setFrom("nkdy50315031@gmail.com");

        String emailContent
                = "<a href='" + BASE_URL + CONFIRM_EMAIL_PATH + "?uuid=" + emailToken.getUuid() + "'>이메일 확인</a>";
        helper.setText(emailContent, true);
        return mimeMessage;
    }

    //유효한 토큰 가져오기
    public EmailToken findByUuidAndExpirationDateAfterAndExpired(String uuid) {
        Optional<EmailToken> emailToken = emailTokenRepository
                .findByUuidAndExpirationDateAfterAndExpired(uuid, LocalDateTime.now(), false);

        // 토큰이 없다면 예외 발생
        return emailToken.orElseThrow(() -> new AccountException(ExceptionType.EMAILTOKEN_IS_EXPIRED));
    }

    //이메일 인증완료시 데이터베이스에서 삭제
    public void deleteEmailTokenByUuid(String uuid) {
        emailTokenRepository.deleteByUuid(uuid);
    }

}