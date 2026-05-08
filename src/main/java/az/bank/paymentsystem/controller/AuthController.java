package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.AdminRequest;
import az.bank.paymentsystem.model.AuthResponse;
import az.bank.paymentsystem.model.LoginRequest;
import az.bank.paymentsystem.model.RegisterRequest;
import az.bank.paymentsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
    @PostMapping("/create-admin")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create admin")
    public AuthResponse createAdmin(@Valid @RequestBody AdminRequest request) {
        return authService.createAdmin(request);
    }
}
