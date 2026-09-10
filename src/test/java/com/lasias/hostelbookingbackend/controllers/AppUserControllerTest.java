package com.lasias.hostelbookingbackend.controllers;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.lasias.hostelbookingbackend.dtos.RegisterNewUserDTO;
import com.lasias.hostelbookingbackend.dtos.UserInformationDTO;
import com.lasias.hostelbookingbackend.models.AppUser;
import com.lasias.hostelbookingbackend.dtos.UpdateUserDTO;
import com.lasias.hostelbookingbackend.repositories.AppUserRepository;
import com.lasias.hostelbookingbackend.services.AppUserService;
import com.lasias.hostelbookingbackend.services.JwtService;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@WireMockTest(httpPort = 8081)
class AppUserControllerTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("booking.service.url", () -> "http://localhost:8081");
    }

    private final String USER_FULL_NAME = "John doe";
    private final String EMAIL = "john.doe@email.com";
    private final String PASSWORD = "JohnDoesPassword!123";
    @Autowired
    private RestClient bookingRestClient;

    @Value("${booking.service.url}")
    private String bookingServiceUrl;

    @Autowired
    private RestClient.Builder restClientBuilder;
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
    void testThatWillPurposelyFail(){
        assertEquals(1,5);
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
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/bookings/active"))
                        .willReturn(WireMock.ok()
                                .withHeader("Content-Type", "application/json")
                                .withBody("false")));


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
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/api/bookings/active")));
    }


    private HttpHeaders getHttpHeadersWithJwtToken() {
        // todo ändra till userid från email.
        Long USER_ID = appUserRepository.findByEmail(EMAIL).get().getId();
        System.out.println(USER_ID);
        String jwtToken = jwtService.generateToken(USER_ID);
        System.out.println(jwtToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "jwt="+jwtToken);
        return headers;
    }



}