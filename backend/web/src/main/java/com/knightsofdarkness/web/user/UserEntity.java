package com.knightsofdarkness.web.user;

import java.util.UUID;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import jakarta.persistence.UniqueConstraint;

// @Entity
// @Table(name = "users", uniqueConstraints = {
//         @UniqueConstraint(columnNames = "username"),
// })
public class UserEntity {

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    public UUID id;

    public String email;
    public String kingdomName;
    public String password;

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
