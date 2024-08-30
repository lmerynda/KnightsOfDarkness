package com.knightsofdarkness.web.user;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserData implements UserDetails {
    public String username;
    public String password;
    public String kingdom;

    public UserData(String username, String password, String kingdom)
    {
        this.username = username;
        this.password = password;
        this.kingdom = kingdom;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return new ArrayList<>();
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    public String getKingdom()
    {
        return kingdom;
    }

    @Override
    public String toString()
    {
        return "UserData{" +
                "username='" + username + '\'' +
                ", kingdom='" + kingdom + '\'' +
                '}';
    }
}
