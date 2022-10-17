package com.sparos.uniquone.msagateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private static String tokenNameOfRequestHeader;

    private static String key;

    private static Long expiredTimeMs;

    private static Long re_expiredTimeMs;

    @Value("${token.name}")
    public void setTokenNameOfRequestHeader(String tokenNameOfRequestHeader) {
        this.tokenNameOfRequestHeader = tokenNameOfRequestHeader;
    }

    @Value("${token.secret}")
    public void setKey(String key) {
        this.key = key;
    }

    @Value("${token.expiration_time}")
    public void setExpiredTimeMs(Long expiredTimeMs) {
        this.expiredTimeMs = expiredTimeMs;
    }

    @Value("${refreshToken.expiration_time}")
    public void setRe_expiredTimeMs(Long re_expiredTimeMs) {
        this.re_expiredTimeMs = re_expiredTimeMs;
    }

    //claims에 넣었던거 받아오기.
    private static Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey(key)).build().parseClaimsJws(token)
                .getBody();
    }

    //서브젝트 비어있는지 유무로 판단함. 조금 더 보안할필요는 있을듯함.
    public static boolean isJwtValid(String token){
        boolean returnValue = true;

        String subject = null;

        try{
            subject = extractClaims(token).getSubject();

        }catch (Exception ex){
            returnValue =false;
        }

        if(subject == null || subject.isEmpty()){
            returnValue = false;
        }

        return returnValue;
    }

    //토큰 유효 시간 검증.
    public static boolean verifyToken(String token) {
        try {
            Date expiredDate = extractClaims(token).getExpiration();
            return expiredDate.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    //데이터 타입 확인해보기.
    public static Long getUserPkId(String token) {
        return extractClaims(token).get("id", Long.class);
    }

    public static String getUserNickName(String token) {
        return extractClaims(token).get("nickName", String.class);
    }

    public static String getUserEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public static String getUserRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public static String getUserRoleByRequest(ServerHttpRequest request) {
//        String token = request.getHeaders(tokenNameOfRequestHeader);
//        String token = request.getHeaders().get(tokenNameOfRequestHeader).toString();
        String token = String.valueOf(request.getHeaders().get(tokenNameOfRequestHeader).get(0));
        return getUserRole(token);
    }

    public static JwtToken generateToken(Long id, String email, String nickName, String role) {
        //닉네임을 여기서 꺼내쓸일이 있을까.
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("id", id);
        claims.put("nickName", nickName);
        claims.put("email", email);
        claims.put("role", role);

        return new JwtToken(
                Jwts.builder()
                        .setClaims(claims)
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(new Date(System.currentTimeMillis() + expiredTimeMs))
                        .signWith(getKey(key), SignatureAlgorithm.HS256)
                        .compact(),
                Jwts.builder()
                        .setClaims(claims)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + re_expiredTimeMs))
                        .signWith(getKey(key), SignatureAlgorithm.HS256)
                        .compact()
        );
    }

    private static Key getKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
