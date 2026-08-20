package com.lasias.hostelbookingbackend.controllers;



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
    public ResponseEntity<String> updateUser(@RequestBody UpdateUserDTO updateUserDTO, @AuthenticationPrincipal Long userId) {
        ResponseCookie jwtCookie = appUserService.updateUser(updateUserDTO, userId);
        if (jwtCookie != null) {
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,jwtCookie.toString()).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<UserInformationDTO> provideUserDetails(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(appUserService.provideUserDetails(userId));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal Long userId) {
        appUserService.deleteMe(userId);
        return ResponseEntity.ok().build();
    }

    
}
