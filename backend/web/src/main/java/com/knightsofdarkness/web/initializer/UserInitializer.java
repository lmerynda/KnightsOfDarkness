package com.knightsofdarkness.web.initializer;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.user.IUserService;
import com.knightsofdarkness.web.user.UserEntity;

@Component
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
        USERS.forEach(user ->
        {
            user.password = passwordEncoder.encode(user.password);
            userService.saveUser(user);
        });
        log.info("users initialized");
    }

    private static final List<UserEntity> USERS = Arrays.asList(
            new UserEntity("uprzejmy", "uprzejmy", "uprzejmy_password"),
            new UserEntity("umbar", "umbar", "umbar_password"),
            new UserEntity("BlacksmithBot", "BlacksmithBot", "1234"),
            new UserEntity("FarmerBot", "FarmerBot", "1234"),
            new UserEntity("IronMinerBot", "IronMinerBot", "1234"),
            new UserEntity("GoldMinerBot", "GoldMinerBot", "1234"));
}
