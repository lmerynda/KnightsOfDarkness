package com.knightsofdarkness.web.User;

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

    public String username;
    public String password;

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
