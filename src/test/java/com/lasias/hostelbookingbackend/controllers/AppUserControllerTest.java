package com.lasias.hostelbookingbackend.controllers;

import com.lasias.hostelbookingbackend.dtos.RegisterNewUserDTO;
import com.lasias.hostelbookingbackend.dtos.UserInformationDTO;
import com.lasias.hostelbookingbackend.models.AppUser;
import com.lasias.hostelbookingbackend.dtos.UpdateUserDTO;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import com.lasias.hostelbookingbackend.services.AppUserService;
import com.lasias.hostelbookingbackend.services.JwtService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AppUserControllerTest {
    //todo laga test
    /*
    private final String USER_FULL_NAME = "John doe";
    private final String EMAIL = "john.doe@email.com";
    private final String PASSWORD = "JohnDoesPassword!123";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Inject
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        // no need since we use a memory database.
        //appUserRepository.deleteAll();
        AppUser user = new AppUser();
        user.setName(USER_FULL_NAME);
        user.setEmail(EMAIL);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole("USER");
        appUserRepository.save(user);
    }

    @AfterEach
    void tearDown(){
        appUserRepository.deleteAll();
    }

    @Test
    void registerUserDeniesFrontendFromAddingDuplicateUsersWithSameInfoAndSuccesfullyAddsNewUsers() {

        RegisterNewUserDTO newUserWithAlreadyRegisteredInformationDTO = new RegisterNewUserDTO(USER_FULL_NAME,EMAIL,PASSWORD);
        ResponseEntity<String> badRegisterResponse = restTemplate.postForEntity("/api/user/register",newUserWithAlreadyRegisteredInformationDTO,String.class);
        assertEquals(HttpStatus.BAD_REQUEST,badRegisterResponse.getStatusCode());


        RegisterNewUserDTO newUserWithGoodInformationDTO = new RegisterNewUserDTO("Joanna Doe","joanna.doe@email.com","JoannaDoesPassword123!");
        ResponseEntity<String> goodBadRegisterResponse = restTemplate.postForEntity("/api/user/register",newUserWithGoodInformationDTO,String.class);
        assertEquals(HttpStatus.OK,goodBadRegisterResponse.getStatusCode());

        assertThat(goodBadRegisterResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .isNotNull()
                .contains("jwt=")
                .contains("Max-Age=")
                .contains("HttpOnly")
                .contains("Path=/");
    }

    @Test
    void updateUserActuallyChangesValuesOfTheFieldsIntended() {

        String newEmail = "newEmail@email.com";
        String newName = "Doe John";
        String newPassword = "aNewPassword!2";
        UpdateUserDTO updateUserDTO = new UpdateUserDTO(newName,newPassword,newEmail,PASSWORD);
        HttpHeaders headers = getHttpHeadersWithJwtToken();
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/user",
                HttpMethod.PATCH,
                new HttpEntity<>(updateUserDTO,headers),
                String.class
        );

        assertEquals(HttpStatus.OK,response.getStatusCode());
        AppUser user = appUserRepository.findByEmail(newEmail).orElse(null);
        assertTrue(appUserRepository.existsByEmail(newEmail));
        assertFalse(appUserRepository.existsByEmail(EMAIL));
        assertNotNull(user);
        assertNotEquals(user.getEmail(),EMAIL);
        assertEquals(newEmail, user.getEmail());
        assertNotEquals(user.getName(), USER_FULL_NAME);
        assertEquals(newName, user.getName());
        assertTrue(appUserService.validPassword(newPassword,user.getPassword()));
        assertFalse(appUserService.validPassword(PASSWORD,user.getPassword()));



    }



    @Test
    void provideUserDetails() {
        String unExpectedEmail = "sadjasdjads@gmail.com";
        String unExpectedName = "Doe John";
        String expectedRole = "USER";
        String unExpectedRole = "ADMIN";

        HttpHeaders headers = getHttpHeadersWithJwtToken();
        ResponseEntity<UserInformationDTO> response = restTemplate.exchange(
                "/api/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserInformationDTO.class
        );
        assertEquals(HttpStatus.OK,response.getStatusCode());
        UserInformationDTO userInformationDTO = response.getBody();
        assertNotNull(userInformationDTO);
        assertEquals(EMAIL,userInformationDTO.email());
        assertNotEquals(unExpectedEmail,userInformationDTO.email());
        assertEquals(USER_FULL_NAME,userInformationDTO.name());
        assertNotEquals(unExpectedName,userInformationDTO.name());
        assertEquals(expectedRole,userInformationDTO.role());
        assertNotEquals(unExpectedRole,userInformationDTO.role());



    }

    @Test
    void deleteUser() {
        assertTrue(appUserRepository.existsByEmail(EMAIL));
        HttpHeaders headers = getHttpHeadersWithJwtToken();
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/user",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertFalse(appUserRepository.existsByEmail(EMAIL));
    }


    private HttpHeaders getHttpHeadersWithJwtToken() {
        // todo ändra till userid från email.
        String jwtToken = jwtService.generateToken(EMAIL);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "jwt="+jwtToken);
        return headers;
    }

*/

}