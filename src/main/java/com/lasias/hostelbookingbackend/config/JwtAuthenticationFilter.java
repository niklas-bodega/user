package com.lasias.hostelbookingbackend.config;

import com.lasias.hostelbookingbackend.dtos.CustomPrincipal;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import com.lasias.hostelbookingbackend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt;
        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : null;

        Cookie jwtCookie = cookies != null ? Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("jwt")).findFirst().orElse(null) : null;
        String bearerToken = request.getHeader("Authorization");

        if (jwtCookie == null) {
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                jwt = bearerToken.substring(7);
            }else {
                log.info("No JWT cookie or bearer token found when authenticating user");
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            jwt = jwtCookie.getValue();
            bearerToken = "Bearer " + jwt;
        }
        final Long userId;
        try {
            userId = jwtService.extractUserId(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                        CustomPrincipal principal = new CustomPrincipal(userId,bearerToken);

                        List<GrantedAuthority> authorities = jwtService.extractAuthorities(jwt);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("User with id {} authenticated.", userId);

            }
        } catch (Exception e) {
            log.error("Invalid JWT token received: {}", e.getMessage());
        }


        filterChain.doFilter(request, response);
    }
}
