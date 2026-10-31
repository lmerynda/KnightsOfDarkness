package com.knightsofdarkness.web.user;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.security.AuthRequestDto;
import com.knightsofdarkness.web.security.AuthResponseDto;
import com.knightsofdarkness.web.security.RegisterRequestDto;
import com.knightsofdarkness.web.security.TokenService;

@RestController
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RegistrationService registrationService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, RegistrationService registrationService)
    {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.registrationService = registrationService;
    }

    @PostMapping("/auth/authenticate")
    public ResponseEntity<Object> login(@RequestBody AuthRequestDto loginRequest)
    {
        var email = loginRequest.email == null ? "" : loginRequest.email.strip().toLowerCase();
        log.info("login request received for user: {}", email);
        var authToken = new UsernamePasswordAuthenticationToken(email, loginRequest.password);
        try
        {
            var authentication = authenticationManager.authenticate(authToken);
            return ResponseEntity.ok(new AuthResponseDto(tokenService.generateToken((UserData) authentication.getPrincipal())));
        } catch (AuthenticationException e)
        {
            log.warn("Authentication failed for user: {} with message: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Wrong email or password"));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequestDto registerRequest)
    {
        log.info("register request received for kingdomName: {}", registerRequest.kingdomName);
        return registrationService.register(registerRequest)
                .map(error -> ResponseEntity.badRequest().body(Map.of("message", error)))
                .orElseGet(() -> ResponseEntity.ok(Map.of("message", "Kingdom founded")));
    }

    @PostMapping("/auth/validate-token")
    public boolean validateToken(@AuthenticationPrincipal UserData currentUser)
    {
        // since this url is protected by security, if we reach here, the token is valid
        log.info("Validate token request received for user: {}", currentUser.email);
        return true;
    }

    /**
     * a valid token is exchanged for a fresh one, the client calls it while the player is active so the session doesn't expire mid-game
     */
    @PostMapping("/auth/refresh")
    public AuthResponseDto refresh(@AuthenticationPrincipal UserData currentUser)
    {
        return new AuthResponseDto(tokenService.generateToken(currentUser));
    }
}
