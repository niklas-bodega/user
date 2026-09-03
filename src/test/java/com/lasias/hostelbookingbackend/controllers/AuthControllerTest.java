package com.lasias.hostelbookingbackend.controllers;

import com.lasias.hostelbookingbackend.dtos.AuthRequestDTO;
import com.lasias.hostelbookingbackend.models.AppUser;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AuthControllerTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("booking.service.url", () -> "http://localhost:8081");
    }
    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "Password1!";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();

        AppUser user = new AppUser();
        user.setName("Test User");
        user.setEmail(EMAIL);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole("USER");
        appUserRepository.save(user);
    }


    @Test
    void loginReturnsJwtCookieWhenCredentialsAreValid() {
        AuthRequestDTO loginCredentials = new AuthRequestDTO(EMAIL, PASSWORD);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", loginCredentials, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("jwt=")
                .contains("HttpOnly")
                .contains("Path=/");
    }

    @Test
    void loginReturnsBadRequestWhenPasswordIsWrong() {
        AuthRequestDTO loginCredentials = new AuthRequestDTO(EMAIL, "Wrongpass1!");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", loginCredentials, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid credentials");
    }

    @Test
    void loginReturnsBadRequestWhenRequestIsInvalid() {
        AuthRequestDTO loginCredentials = new AuthRequestDTO("not-an-email", "short");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", loginCredentials, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("email")
                .contains("password");
    }


    @Test
    void logoutClearsJwtCookieWhenUserIsAuthenticated() {
        AuthRequestDTO loginCredentials = new AuthRequestDTO(EMAIL, PASSWORD);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login", loginCredentials, String.class);
        String jwtCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";", 2)[0];
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, jwtCookie);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/logout",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("jwt=;")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("Path=/");
    }
}
