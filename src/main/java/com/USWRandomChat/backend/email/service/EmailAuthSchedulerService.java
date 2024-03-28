package com.USWRandomChat.backend.email.service;

import com.USWRandomChat.backend.email.repository.EmailTokenRepository;
import com.USWRandomChat.backend.email.domain.EmailToken;
import com.USWRandomChat.backend.member.secure.service.MemberTempService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthSchedulerService {

    //이메일 토큰 만료 5분
    //토큰 만료, 30분 후 임시회원 삭제

    // 이메일 토큰 만료 시간
    private static final long EMAIL_TOKEN_AFTER_EXPIRATION_TIME_VALUE = 30L;

    private final EmailTokenRepository emailTokenRepository;
    private final MemberTempService memberTempService;

    public List<EmailToken> findExpiredFalse(LocalDateTime localDateTime) {
        return emailTokenRepository.findByExpirationDateBeforeAndExpiredIsFalse(localDateTime.minusMinutes(EMAIL_TOKEN_AFTER_EXPIRATION_TIME_VALUE));
    }

    //유령회원 지우기
    @Transactional
    @Scheduled(cron = "0 * * * * * ")
    public void removeMember() {
        log.info("{} - 이메일 인증을 수행하지 않은 유저 검증 시작", LocalDateTime.now());
        List<EmailToken> emailTokens = findExpiredFalse(LocalDateTime.now());

        for (EmailToken emailToken : emailTokens) {
            Long account = emailToken.getMemberTemp().getId();
            memberTempService.deleteFromId(account);

        }
        log.info("{} - 이메일 인증을 수행하지 않은 유저 검증 종료", LocalDateTime.now());
    }
}