package com.lasias.hostelbookingbackend.config;

import com.lasias.hostelbookingbackend.models.AppUser;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    //private final AppUserRepository appUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : null;
        Cookie jwtCookie = cookies != null ? Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("jwt")).findFirst().orElse(null) : null;
        String bearerToken = request.getHeader("Authorization");


        String jwt;
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
        }
        final String email;
        final LocalDateTime issuedAt;

        /*
        try {
            email = jwtService.extractEmail(jwt);
            issuedAt = jwtService.extractIAT(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<AppUser> user = appUserRepository.findByEmail(email);
                if (user.isPresent()) {
                    LocalDateTime denyTokensPriorTo = user.get().getDenyTokensPriorTo();
                    if (denyTokensPriorTo != null) {
                        log.info(denyTokensPriorTo.toString());
                    } else {
                        log.info("User has no 'denyTokensPriorTo' date");
                    }
                    boolean isTokenValid = denyTokensPriorTo == null ||
                            !issuedAt.isBefore(denyTokensPriorTo.truncatedTo(ChronoUnit.SECONDS));

                    if (isTokenValid) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.get(), null, user.get().getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("User authenticated: {}", user.get().getEmail());
                    } else {
                        log.error("JWT token issued before the user's 'denyTokensPriorTo' date");
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Invalid JWT token received: {}", e.getMessage());
        }

         */
        filterChain.doFilter(request, response);
    }
}
