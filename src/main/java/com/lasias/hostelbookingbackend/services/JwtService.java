package com.lasias.hostelbookingbackend.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    public String generateToken(String email){
        int expiryTime = 86400000;
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public String extractEmail(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Cookie createJwtCookie(String email){
        Cookie cookie = new Cookie("jwt",generateToken(email));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setSecure(false); // todo sätt till true när vi kör https.
        return cookie;
    }

    public ResponseCookie createJwtCookie(String email, boolean logoutCookie){
        return ResponseCookie.from("jwt", logoutCookie ? "" : generateToken(email))
                .httpOnly(true)
                .path("/")
                .maxAge(logoutCookie ? 0 : (60 * 60 * 24))
                .secure(false) // todo sätt till true när vi kör https.
                .build();
    }

    public String createBearerToken(String jwt){
        return "Bearer " + jwt;
    }

    public LocalDateTime extractIAT(String jwt) {
        return LocalDateTime.ofInstant(Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(jwt).getBody().getIssuedAt().toInstant(), java.time.ZoneId.systemDefault());
    }
}
