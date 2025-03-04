package com.knightsofdarkness.web.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserEntity {
    @Id
    @Column(nullable = false, unique = true)
    public String email;
    public String kingdomName;
    public String password;

    public UserEntity()
    {
    }

    public UserEntity(String email, String kingdomName, String password)
    {
        this.email = email;
        this.kingdomName = kingdomName;
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "UserEntity [email=" + email + ", kingdomName=" + kingdomName + "]";
    }
}
