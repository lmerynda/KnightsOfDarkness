package com.knightsofdarkness.web.user;

import java.util.Optional;

import java.util.List;

public interface IUserService {
    List<UserEntity> getUsers();

    Optional<UserEntity> getUserByEmail(String username);

    boolean hasUserWithEmail(String username);

    UserEntity saveUser(UserEntity user);

    void deleteUser(UserEntity user);
}
