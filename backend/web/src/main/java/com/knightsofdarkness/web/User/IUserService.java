package com.knightsofdarkness.web.User;

import java.util.Optional;

import java.util.List;

public interface IUserService {
    List<UserEntity> getUsers();

    Optional<UserEntity> getUserByUsername(String username);

    boolean hasUserWithUsername(String username);

    UserEntity saveUser(UserEntity user);

    void deleteUser(UserEntity user);
}
