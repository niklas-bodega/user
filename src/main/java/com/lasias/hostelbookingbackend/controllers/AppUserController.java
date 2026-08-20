package com.lasias.hostelbookingbackend.controllers;



import com.lasias.hostelbookingbackend.dtos.RegisterNewUserDTO;
import com.lasias.hostelbookingbackend.dtos.UserInformationDTO;
import com.lasias.hostelbookingbackend.dtos.UserPrincipal;
import com.lasias.hostelbookingbackend.models.AppUser;
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
    public ResponseEntity<String> updateUser(@RequestBody UpdateUserDTO updateUserDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResponseCookie jwtCookie = appUserService.updateUser(updateUserDTO, user);
        if (jwtCookie != null) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,jwtCookie.toString()).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<UserInformationDTO> provideUserDetails(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(appUserService.provideUserDetails(user));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal AppUser user) {
        appUserService.deleteMe(user);
        return ResponseEntity.ok().build();
    }

    
}
