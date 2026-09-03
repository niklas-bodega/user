package com.lasias.hostelbookingbackend.services;

import com.lasias.hostelbookingbackend.dtos.AuthRequestDTO;
import com.lasias.hostelbookingbackend.dtos.RegisterNewUserDTO;
import com.lasias.hostelbookingbackend.models.AppUser;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RequiredArgsConstructor
class AppUserServiceTest {

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Inject
    private AppUserService appUserService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("booking.service.url", () -> "http://localhost:8081");
    }

    @BeforeEach
    public void setUp() {
        appUserRepository.deleteAll();
        appUserService.register(new RegisterNewUserDTO("Admin User", "admin2@test.com", "admin123"));
        appUserService.register(new RegisterNewUserDTO("John Doe", "john@test.com", "password123"));
        appUserService.register(new RegisterNewUserDTO("Jane Smith", "jane@test.com", "password123"));
        appUserService.register(new RegisterNewUserDTO("Manager Mike", "manager@test.com", "manager123"));

    }

    @Test
    void registerOauth2() {
        long expectedCount = appUserRepository.count()+1;
        appUserService.register("Jonathan Doe","jonathans@gmail.com",null,null);
        assertEquals(appUserRepository.count(), expectedCount);
        assertThrows(IllegalArgumentException.class, () -> appUserService.register("Jonathan Doe","jonathans@gmail.com",null,null));
    }

    @Test
    void testRegister() {
        long expectedCount = appUserRepository.count()+1;
        RegisterNewUserDTO dto = new RegisterNewUserDTO(
                "Jane Simpson",
                "janesimpson@gmail.ru",
                "AverYLongPassWordWithOtherStuff!2312-.,"
        );
        appUserService.register(dto);
        assertEquals(appUserRepository.count(), expectedCount);
        assertThrows(IllegalArgumentException.class, () -> appUserService.register(dto));
    }

    @Test
    void loginUser() {
        String email = "test@email.com";
        String password = "password12371237!";
        String fullname = "Along Andvalidname";
        appUserService.register(new RegisterNewUserDTO(fullname,email,password));

        assertThrows(IllegalArgumentException.class, () -> appUserService.loginUser(null));
        assertThrows(IllegalArgumentException.class, () -> appUserService.loginUser(new AuthRequestDTO("email@gmail.com",null)));
        assertNotNull(appUserService.loginUser(new AuthRequestDTO(email,password)));
    }

    @Test
    void validPassword() {
        AppUser user = appUserRepository.findByEmail("manager@test.com").get();
        assertTrue(appUserService.validPassword("manager123",user.getPassword()));
    }

    @Test
    void passwordMatchesCriteria() {
        assertTrue(appUserService.passwordMatchesCriteria("Abcdefg1!"));
        assertFalse(appUserService.passwordMatchesCriteria("abcdefg1!"));
        assertFalse(appUserService.passwordMatchesCriteria(""));
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteMe() {
    }

    @Test
    void provideUserDetails() {
    }

    @Test
    void logout() {
    }
}