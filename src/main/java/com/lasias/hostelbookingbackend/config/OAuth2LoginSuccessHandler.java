package com.lasias.hostelbookingbackend.config;

import com.lasias.hostelbookingbackend.services.AppUserService;
import com.lasias.hostelbookingbackend.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final AppUserService appUserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User != null ? oAuth2User.getAttribute("email") : null;
        if (email == null){
            email = (oAuth2User != null ? oAuth2User.getAttribute("login") : null) + "@github.com";
        }
        // todo sätt en variabel för server adress
        String frontendUrl = "https://niklasbodega.lasias.com/oauth2/redirect";

        Long userId = appUserService.userIdFromEmail(email);

        Cookie cookie = jwtService.createJwtCookie(userId);
        response.addCookie(cookie);
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
