package com.knightsofdarkness.web.user;

import java.util.Optional;

import java.util.List;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.storage.user.UserEntity;
import com.knightsofdarkness.storage.user.UserJpaRepository;

@Service
public class UserService implements IUserService {

    private final UserJpaRepository userRepository;

    public UserService(UserJpaRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserEntity> getUsers()
    {
        return userRepository.findAll();
    }

    @Override
    public Optional<UserEntity> getUserByEmail(String email)
    {
        return userRepository.findById(email);
    }

    @Override
    public boolean hasUserWithEmail(String email)
    {
        return userRepository.existsById(email);
    }

    @Override
    public UserEntity saveUser(UserEntity user)
    {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UserEntity user)
    {
        userRepository.delete(user);
    }
}
