package com.pod.back.controllers;

import com.pod.back.dto.AuthResponse;
import com.pod.back.dto.LoginRequest;
import com.pod.back.dto.RegisterRequest;
import com.pod.back.dto.ResendConfirmationRequest;
import com.pod.back.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Inscription réussie. Vérifiez vos e-mails."));
    }

    @PostMapping("/resendConfirmation")
    public ResponseEntity<String> resendConfirmation(@Valid @RequestBody ResendConfirmationRequest request) {
        authService.resendConfirmationEmail(request.email());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/confirm")
    public ResponseEntity<Map<String,String>> confirmAccount(@RequestParam("token") String token) {
        authService.confirmAccount(token);
        return ResponseEntity.ok(Map.of("message","compte confirmé"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


}