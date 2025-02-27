package com.knightsofdarkness.web.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.security.AuthRequestDto;
import com.knightsofdarkness.web.security.AuthResponseDto;
import com.knightsofdarkness.web.security.RegisterRequestDto;
import com.knightsofdarkness.web.security.TokenService;
import com.knightsofdarkness.web.user.model.UserEntity;

@RestController
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final KingdomService kingdomService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, IUserService userService, PasswordEncoder passwordEncoder, KingdomService kingdomService)
    {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.kingdomService = kingdomService;
    }

    @PostMapping("/auth/authenticate")
    public AuthResponseDto login(@RequestBody AuthRequestDto loginRequest)
    {
        log.info("login request received for user: {}", loginRequest.email);
        var authToken = new UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password);
        try
        {
            Authentication authentication = authenticationManager.authenticate(authToken);
            String token = tokenService.generateToken(authentication);
            return new AuthResponseDto(token);
        } catch (AuthenticationException e)
        {
            log.warn("Authentication failed for user: {} with message: {}", loginRequest.email, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDto registerRequest)
    {
        log.info("register request received for email: {} and kingdomName: {}", registerRequest.email, registerRequest.kingdomName);

        if (userService.hasUserWithEmail(registerRequest.email))
        {
            throw new IllegalArgumentException("email already exists"); // TODO return a proper error response
        }

        UserData user = new UserData(registerRequest.email, registerRequest.kingdomName, registerRequest.password);
        user.password = passwordEncoder.encode(user.getPassword());

        UserEntity newUser = new UserEntity(user.email, user.getKingdomName(), user.getPassword());
        userService.saveUser(newUser);
        // TODO rework bring back
        // kingdomService.createKingdom(user.getKingdomName());

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/auth/validate-token")
    public boolean validateToken(@AuthenticationPrincipal UserData currentUser)
    {
        // since this url is protected by security, if we reach here, the token is valid
        log.info("Validate token request received for user: {}", currentUser.email);
        return true;
    }
}
