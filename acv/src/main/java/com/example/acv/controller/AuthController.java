package com.example.acv.controller;

import com.example.acv.dto.request.LoginRequest;
import com.example.acv.dto.request.RefreshRequest;
import com.example.acv.dto.response.AuthResponse;
import com.example.acv.dto.response.UserResponse;
import com.example.acv.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Không có quyền truy cập"));
        }

        UserResponse response = authService.me(auth.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody(required = false) RefreshRequest request,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(authService.refresh(request, authHeader));
    }
}
