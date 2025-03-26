package com.knightsofdarkness.web.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.web.user.IUserService;
import com.knightsofdarkness.web.user.UserData;
import com.knightsofdarkness.web.user.model.UserEntity;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUserService userService;

    public UserDetailsServiceImpl(IUserService userService)
    {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
    {
        UserEntity user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("email %s not found", email)));
        return mapUserToCustomUserDetails(user);
    }

    private UserData mapUserToCustomUserDetails(UserEntity user)
    {
        return new UserData(user.email, user.kingdomName, user.password);
    }
}
