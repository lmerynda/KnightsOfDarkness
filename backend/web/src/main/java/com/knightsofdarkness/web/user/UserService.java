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
    public Optional<UserEntity> getUserByUsername(String username)
    {
        return users.stream().filter(user -> user.username.equals(username)).findFirst();
    }

    @Override
    public boolean hasUserWithUsername(String username)
    {
        return users.stream().anyMatch(user -> user.username.equals(username));
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
