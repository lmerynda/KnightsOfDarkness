package com.knightsofdarkness.web.User;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.Security.AuthRequestDto;
import com.knightsofdarkness.web.Security.AuthResponseDto;
import com.knightsofdarkness.web.Security.TokenService;


@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(IUserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService)
    {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/auth/authenticate")
    public AuthResponseDto login(@RequestBody AuthRequestDto loginRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password);
        Authentication authentication = authenticationManager.authenticate(authToken);
        String token = tokenService.generateToken(authentication);
        return new AuthResponseDto(token);
    }
}
