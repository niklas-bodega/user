package com.lasias.hostelbookingbackend.services;

import com.lasias.hostelbookingbackend.models.AppUser;
import com.lasias.hostelbookingbackend.enums.AuthProvider;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {
    private final AppUserService appUserService;
    private final AppUserRepository appUserRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException{
        OidcUser oidcUser = super.loadUser(userRequest);

        String providerName = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        Optional<AppUser> existingUser = appUserRepository.findByEmail(email);

        if (existingUser.isPresent()){
            log.info("User logged in with email: {}, authenticated by {}", existingUser.get().getEmail(),providerName);
        }
        else
        {
            AuthProvider authProvider = AuthProvider.valueOf(providerName);
            String authProviderId = oidcUser.getName();
            appUserService.register(name,email,authProviderId,authProvider);
            log.info("New user registered with email: {}, authenticated by {}", email,providerName);
        }
        return oidcUser;
    }
}
