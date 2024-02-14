package com.USWRandomChat.backend.security.jwt;

import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.security.domain.Authority;
import com.USWRandomChat.backend.security.jwt.service.JpaUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtProvider {

    @Value("${jwt.secret.key}")
    private String salt;

    private Key secretKey;

    // 만료시간 30분
    private final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 30;

    private final JpaUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public String createToken(String memberId, List<Authority> roles) {
        Claims claims = Jwts.claims().setSubject(memberId);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 권한정보 획득
    // Spring Security 인증과정에서 권한확인을 위한 기능
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getMemberId(accessToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에 담겨있는 memberId 획득
    public String getMemberId(String accessToken) {
        // 만료된 토큰에 대해 parseClaimsJws를 수행하면 io.jsonwebtoken.ExpiredJwtException이 발생한다.
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            return e.getClaims().getSubject();
        }
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
    }

    // Authorization Header를 통해 인증을 한다.
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    // 토큰 검증
    public boolean validateToken(String accessToken) {
        try {
            // Bearer 검증
            if (!accessToken.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
                throw new AccountException(ExceptionType.INVALID_TOKEN_FORMAT);
            } else {
                accessToken = accessToken.split(" ")[1].trim();
            }

            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);

            if (claims.getBody().getExpiration().before(new Date())) {
                throw new AccountException(ExceptionType.TOKEN_IS_EXPIRED);
            }
            return true;
        } catch (ExpiredJwtException e) {
            throw new AccountException(ExceptionType.TOKEN_IS_EXPIRED);
        }
    }
}