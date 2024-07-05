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
    public String kingdom;

    public UserEntity(String username, String password, String kingdom)
    {
        this.username = username;
        this.password = password;
        this.kingdom = kingdom;
    }

    public String toString()
    {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", kingdom='" + kingdom + '\'' +
                '}';
    }
}
