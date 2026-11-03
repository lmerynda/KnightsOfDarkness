package com.knightsofdarkness.web.initializer;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.user.IUserService;
import com.knightsofdarkness.web.user.model.UserEntity;

/**
 * development accounts for the seeded kingdoms, never in production
 */
@Component
@ConditionalOnProperty(name = "game.seed-dev-accounts", havingValue = "true")
public class UserInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(UserInitializer.class);

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserInitializer(IUserService userService, PasswordEncoder passwordEncoder)
    {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args)
    {
        // the accounts outlive the server, so a restart keeps them as they are
        USERS.stream().filter(user -> !userService.hasUserWithEmail(user.email))
                .forEach(user -> userService.saveUser(new UserEntity(user.email, user.kingdomName, passwordEncoder.encode(user.password))));
        log.info("users initialized");
    }

    private static final List<UserEntity> USERS = Arrays.asList(
            new UserEntity("uprzejmy", "uprzejmy", "uprzejmy_password"),
            new UserEntity("umbar", "Umbar", "umbar_password"));
}
