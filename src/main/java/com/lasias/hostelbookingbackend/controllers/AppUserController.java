package com.lasias.hostelbookingbackend.controllers;



import com.lasias.hostelbookingbackend.dtos.CustomPrincipal;
import com.lasias.hostelbookingbackend.dtos.RegisterNewUserDTO;
import com.lasias.hostelbookingbackend.dtos.UserInformationDTO;
import com.lasias.hostelbookingbackend.dtos.UpdateUserDTO;
import com.lasias.hostelbookingbackend.services.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterNewUserDTO newUser) {
        ResponseCookie jwtCookie = appUserService.register(newUser);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,jwtCookie.toString()).build();
    }

    @PatchMapping
    public ResponseEntity<String> updateUser(@RequestBody UpdateUserDTO updateUserDTO, @AuthenticationPrincipal CustomPrincipal principal) {
        ResponseCookie jwtCookie = appUserService.updateUser(updateUserDTO, principal.userID());
        if (jwtCookie != null) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,jwtCookie.toString()).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<UserInformationDTO> provideUserDetails(@AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(appUserService.provideUserDetails(principal.userID()));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomPrincipal principal) {

        appUserService.deleteMe(principal);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exist")
    public ResponseEntity<String> doesUserExistOrDidTheyDeleteThemSelvesAndTryingToMakeABookingWithStillValidToken(){
        return ResponseEntity.notFound().build();
    }
}
