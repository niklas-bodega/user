package com.lasias.hostelbookingbackend.services;

import com.lasias.hostelbookingbackend.dtos.*;
import com.lasias.hostelbookingbackend.models.AppUser;
import com.lasias.hostelbookingbackend.enums.AuthProvider;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestClient bookingRestClient;


    // register user through OAuth2 providers
    public void register(String name, String email, String authProviderId, AuthProvider authProvider) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            log.error("Registration failed, email already in use:");
            throw new IllegalArgumentException("Email already in use");
        }
        AppUser user = new AppUser();
        user.setName(name);
        user.setEmail(email);
        user.setAuthProvider(authProvider);
        user.setAuthProviderId(authProviderId);
        user.setRole("USER");
        log.info("New user registered: {}", user.getEmail());
        appUserRepository.save(user);
    }

    // register user through user information from the frontend.
    public ResponseCookie register(RegisterNewUserDTO newUser) {
        if (appUserRepository.findByEmail(newUser.email()).isPresent()) {
            log.error("Registration failed, email already in use:");
            throw new IllegalArgumentException("Email already in use");
        }
        AppUser user = new AppUser();
        user.setName(newUser.fullName());
        user.setEmail(newUser.email());
        user.setPassword(hashPassword(newUser.password()));
        user.setRole("USER");
        appUserRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        Long userId = appUserRepository.findByEmail(newUser.email()).get().getId();
        return jwtService.createJwtCookie(userId, false);
    }


    // login user without OAuth2 providers
    public ResponseCookie loginUser(AuthRequestDTO request) {
        if (request == null) {
            log.error("Local login request is null whe loginUser is called");
            throw new IllegalArgumentException("Request is null");
        }
        String email = request.email();
        String password = request.password();
        if (email == null || password == null) {
            log.error("Login failed, email and password are required");
            throw new IllegalArgumentException("Email and password are required");
        }
        AppUser user = appUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found."));
        if (validPassword(password, user.getPassword())) {
            log.info("User logged in: {}", user.getEmail());
            return jwtService.createJwtCookie(user.getId(), false);
        }
        log.error("Login failed, invalid credentials");
        throw new IllegalArgumentException("Invalid credentials");
    }

    public boolean validPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }

    public boolean passwordMatchesCriteria(String password) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(regex);
    }

    private boolean isValidEmail(String email) {
        // Ett robust regex som tillåter moderna domäner (fler än 6 tecken)
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
        return email.matches(regex);
    }


    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public ResponseCookie updateUser(UpdateUserDTO updateUserDTO, Long userId) {
        ResponseCookie newCookie = null;

        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (updateUserDTO.name() != null) {
            String newName = updateUserDTO.name();
            if (newName.length() >= 3 && newName.length() <= 50) {
                user.setName(updateUserDTO.name());
            } else {
                throw new IllegalArgumentException("Name length must be between 3 and 50");
            }
        }
        if (updateUserDTO.email() != null) {
            String newEmail = updateUserDTO.email();
            if (!isValidEmail(newEmail)) {
                log.error("Invalid email format");
                throw new IllegalArgumentException("Invalid email format");
            }
            user.setEmail(newEmail);
            newCookie = jwtService.createJwtCookie(user.getId(), false);
        }
        if (updateUserDTO.password() != null) {
            // if the user is updating their password, check if the current password is correct. If there is no current password, the user is updating their password for the first time.
            if (user.getPassword() == null || validPassword(updateUserDTO.currentPassword(), user.getPassword())) {
                String newPassword = updateUserDTO.password();
                if (!passwordMatchesCriteria(newPassword)) {
                    log.error("User did not provide a valid password. Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character");
                    throw new IllegalArgumentException("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character");
                }
                user.setPassword(hashPassword(newPassword));
            }
        }
        log.info("User updated: {}", user.getEmail());
        appUserRepository.save(user);
        return newCookie;
    }


    public void deleteMe(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Unable to delete user, User not found"));

        //todo anropa booking mikroservice gällande om det finns aktiva bokningar
        // TODO SE TILL ATT DET FUNGERAR. / FÅ DET ATT FUNGERA.
        boolean userHasActiveBooking = Boolean.TRUE.equals(bookingRestClient
                .get()
                .uri("/api/bookings/user-has-upcomming-bookings/{id}", user.getId())
                .header("Authorization", jwtService.createBearerToken(user.getEmail()))
                .retrieve()
                .body(boolean.class));

        if (userHasActiveBooking) {
            log.error("Unable to delete user, User has bookings");
            throw new IllegalArgumentException("Unable to delete user, User has bookings");
        }



        // //todo anropa bookingservice för att rensa bort användarid från historiska bokningar.
        // TODO SE TILL ATT DET FUNGERAR. / FÅ DET ATT FUNGERA.
        bookingRestClient.get()
                .uri("/api/bookings/remove-userid-from-passed-bookings/{id}", user.getId())
                .header("Authorization", jwtService.createBearerToken(user.getEmail()))
                .retrieve()
                .body(boolean.class);

            log.info("User deleted: {}", user.getEmail());
            appUserRepository.delete(user);

    }

    public UserInformationDTO provideUserDetails(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found."));
        log.info("User details retrieved: {}", user.getEmail());
        return new UserInformationDTO(user.getEmail(), user.getName(), user.getRole(), user.getCreatedAt(), (user.getPassword() != null));
    }

    public ResponseEntity<String> logout(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User not found when logging out");
            throw new IllegalArgumentException("User not found");
        }
        ResponseCookie cookie = jwtService.createJwtCookie(user.getId(), true);

        SecurityContextHolder.clearContext();
        log.info("User logged out: {}", user.getEmail());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    public ResponseEntity<String> logoutAllDevices(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User not found when logging out all devices");
            throw new IllegalArgumentException("User not found");
        }
        user.setDenyTokensPriorTo(LocalDateTime.now(ZoneId.of("Europe/Copenhagen")).truncatedTo(ChronoUnit.SECONDS));
        appUserRepository.save(user);
        ResponseCookie cookie = jwtService.createJwtCookie(user.getId(), true);

        SecurityContextHolder.clearContext();
        log.info("User logged out all devices: {} and tokens issued previously will be rejected.", user.getEmail());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    public Long userIdFromEmail(String email) {
        return appUserRepository.findByEmail(email).map(AppUser::getId).orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
