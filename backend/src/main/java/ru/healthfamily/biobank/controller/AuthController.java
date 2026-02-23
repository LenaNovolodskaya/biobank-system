package ru.healthfamily.biobank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.healthfamily.biobank.dto.AuthResponse;
import ru.healthfamily.biobank.dto.CurrentUserResponse;
import ru.healthfamily.biobank.dto.LoginRequest;
import ru.healthfamily.biobank.dto.RegisterRequest;
import ru.healthfamily.biobank.security.CustomUserDetails;
import ru.healthfamily.biobank.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new CurrentUserResponse(
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getUserId(),
                userDetails.getPermissionNames()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
