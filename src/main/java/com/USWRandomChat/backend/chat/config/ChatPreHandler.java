package com.USWRandomChat.backend.chat.config;

import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import io.jsonwebtoken.MalformedJwtException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChatPreHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            log.info("stomp intercepter 실행");
            StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            assert headerAccessor != null;
            String authorizationHeader = String.valueOf(headerAccessor.getNativeHeader("Authorization"));

            StompCommand command = headerAccessor.getCommand();

            assert command != null;
            if(command.equals(StompCommand.UNSUBSCRIBE) || command.equals(StompCommand.MESSAGE) ||
                    command.equals(StompCommand.CONNECTED) || command.equals(StompCommand.SEND)){
                return message;
            }
            else if (command.equals(StompCommand.ERROR)) {
                throw new MessageDeliveryException("error");
            }

            if (authorizationHeader == null) {
                log.info("chat header가 없는 요청입니다.");
                throw new MalformedJwtException("jwt");
            }

            // JWT 검증
            String accessToken = authorizationHeader.replace("Bearer ", "");
            jwtProvider.validateAccessToken(accessToken);

            // Principal 설정
            String username = jwtProvider.getAccount(accessToken);
            headerAccessor.setUser(() -> username);

        } catch (Exception e) {
            log.error("JWT 에러: ", e);
            throw new MalformedJwtException("jwt");
        }
        return message;
    }
}
