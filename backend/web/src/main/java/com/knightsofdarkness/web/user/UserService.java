package com.knightsofdarkness.web.user;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    List<UserEntity> users = new ArrayList<>();

    @Override
    public List<UserEntity> getUsers()
    {
        return users;
    }

    @Override
    public Optional<UserEntity> getUserByEmail(String email)
    {
        return users.stream().filter(user -> user.email.equals(email)).findFirst();
    }

    @Override
    public boolean hasUserWithEmail(String email)
    {
        return users.stream().anyMatch(user -> user.email.equals(email));
    }

    @Override
    public UserEntity saveUser(UserEntity user)
    {
        users.add(user);
        return user;
    }

    @Override
    public void deleteUser(UserEntity user)
    {
        users.remove(user);
    }
}
