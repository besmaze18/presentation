package com.fittrack.auth.api;

import com.fittrack.auth.dto.AuthResponse;
import com.fittrack.auth.dto.LoginRequest;
import com.fittrack.auth.dto.RegisterRequest;
import com.fittrack.auth.service.AuthService;
import com.fittrack.auth.service.RefreshCookieFactory;
import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Registration, login, token refresh and logout")
public class AuthController {

    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;

    public AuthController(AuthService authService, RefreshCookieFactory refreshCookieFactory) {
        this.authService = authService;
        this.refreshCookieFactory = refreshCookieFactory;
    }

    @PostMapping("/register")
    @Operation(summary = "Create an account and start a session")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.RegistrationResult result = authService.register(request);
        return respond(result, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Exchange credentials for an access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.RegistrationResult result = authService.login(request);
        return respond(result, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh cookie and issue a fresh access token")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "${fittrack.security.jwt.cookie-name:fittrack_refresh}", required = false)
                    String refreshToken,
            HttpServletRequest request) {
        AuthService.RegistrationResult result = authService.refresh(refreshToken);
        return respond(result, HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the current refresh-token family and clear the cookie")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${fittrack.security.jwt.cookie-name:fittrack_refresh}", required = false)
                    String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expired().toString())
                .build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Revoke every refresh token belonging to the authenticated user")
    public ResponseEntity<Void> logoutAll(@CurrentUser AuthenticatedUser currentUser) {
        authService.logoutAllSessions(currentUser.getId());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.expired().toString())
                .build();
    }

    private ResponseEntity<AuthResponse> respond(AuthService.RegistrationResult result, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookieFactory.create(result.tokens().refreshToken()).toString())
                .body(AuthResponse.of(result.tokens(), UserResponse.from(result.user())));
    }
}
