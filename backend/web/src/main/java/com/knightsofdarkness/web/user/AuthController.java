package com.knightsofdarkness.web.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.security.AuthRequestDto;
import com.knightsofdarkness.web.security.AuthResponseDto;
import com.knightsofdarkness.web.security.TokenService;

@RestController
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(IUserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService)
    {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/auth/authenticate")
    public AuthResponseDto login(@RequestBody AuthRequestDto loginRequest)
    {
        log.info("login request received for user: {}", loginRequest.username);
        var authToken = new UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password);
        try
        {
            Authentication authentication = authenticationManager.authenticate(authToken);
            String token = tokenService.generateToken(authentication);
            return new AuthResponseDto(token);
        } catch (AuthenticationException e)
        {
            log.warn("Authentication failed for user: {} with message: {}", loginRequest.username, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/auth/validate-token")
    public boolean validateToken(@AuthenticationPrincipal UserData currentUser)
    {
        // since this url is protected by security, if we reach here, the token is valid
        log.info("Validate token request received for user: {}", currentUser.username);
        return true;
    }
}
