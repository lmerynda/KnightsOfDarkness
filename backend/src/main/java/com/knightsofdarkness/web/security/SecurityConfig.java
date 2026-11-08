package com.knightsofdarkness.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * everything asks for a signed-in ruler except what a visitor needs: the health check, signing in or founding a
     * kingdom, and the landing page's era, monuments and rankings
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, Environment environment) throws Exception
    {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests ->
                {
                    requests.requestMatchers(HttpMethod.GET, "/health", "/game/era", "/game/monuments", "/game/rankings").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/authenticate", "/auth/register", "/auth/trial").permitAll()
                            .requestMatchers("/error").permitAll();
                    // the simulation harness drives its throwaway worlds without signing in
                    if (environment.matchesProfiles("simulation"))
                    {
                        requests.requestMatchers("/simulation/**").permitAll();
                    }
                    requests.anyRequest().authenticated();
                })
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .cors(Customizer.withDefaults())
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
