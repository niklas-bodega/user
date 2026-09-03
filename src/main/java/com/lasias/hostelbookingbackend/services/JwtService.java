package com.lasias.hostelbookingbackend.services;

import com.lasias.hostelbookingbackend.models.AppUser;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final AppUserRepository appUserRepository;

    @Value("${app.jwt.secret}")
    private String secretKey;

    public String generateToken(Long userId){
        int expiryTime = 86400000;
        AppUser user = appUserRepository.findById(userId).orElseThrow();
        return Jwts.builder().
                subject(userId.toString())
                //todo rensa bort eller implementera
                .claim("role", "ROLE_" + user.getRole())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Long extractUserId(String token){
        return Long.parseLong(Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

    public List<GrantedAuthority> extractAuthorities(String token){
        String role = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);

        if (role == null || role.isBlank()){
            return Collections.emptyList();
        }
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return List.of(new SimpleGrantedAuthority(authority));
    }


    public Cookie createJwtCookie(Long userId){
        Cookie cookie = new Cookie("jwt",generateToken(userId));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setSecure(false); // todo sätt till true när vi kör https.
        return cookie;
    }

    public ResponseCookie createJwtCookie(Long userId, boolean logoutCookie){
        return ResponseCookie.from("jwt", logoutCookie ? "" : generateToken(userId))
                .httpOnly(true)
                .path("/")
                .maxAge(logoutCookie ? 0 : (60 * 60 * 24))
                .secure(false) // todo sätt till true när vi kör https.
                .build();
    }

    public String createBearerToken(String jwt){
        return "Bearer " + jwt;
    }

}
