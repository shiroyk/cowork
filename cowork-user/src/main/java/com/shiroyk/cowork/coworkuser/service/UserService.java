package com.shiroyk.cowork.coworkuser.service;

import com.google.common.collect.Streams;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkuser.model.User;
import com.shiroyk.cowork.coworkuser.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public Optional<User> findUserByName(String name) {
        return userRepository.findUserByUsername(name);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public Long count() {
        return userRepository.count();
    }

    public Page<User> findAllUser(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<UserDto> findUserDtoById(String id) {
        return userRepository.findById(id).map(User::toUserDtoL);
    }

    public User findUserById(String id) {
        return userRepository.findUserById(id);
    }

    public List<User> findUsersByUsernameContains(String name) {
        return userRepository.findUsersByUsernameContains(name);
    }

    public List<UserDto> findUsersByGroup(String group, Pageable pageable) {
        return userRepository.findUsersByGroup(group, pageable)
                .stream().map(User::toUserDtoM)
                .collect(Collectors.toList());
    }

    public List<UserDto> findUserDtoListById(List<String> users) {
        return Streams.stream(userRepository.findAllById(users))
                .map(User::toUserDtoL)
                .collect(Collectors.toList());
    }

    public User save(User user) {
        user.setUpdateTime();
        return userRepository.save(user);
    }
}
