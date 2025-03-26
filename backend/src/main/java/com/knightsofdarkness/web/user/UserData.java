package com.knightsofdarkness.web.user;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserData implements UserDetails {
    public String email;
    public String kingdomName;
    public String password;

    public UserData(String email, String kingdomName, String password)
    {
        this.email = email;
        this.kingdomName = kingdomName;
        this.password = password;
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

    public String getEmail()
    {
        return email;
    }

    public String getKingdomName()
    {
        return kingdomName;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return email;
    }

    @Override
    public String toString()
    {
        return "UserData [email=" + email + ", kingdomName=" + kingdomName + ", password=" + password + "]";
    }
}
