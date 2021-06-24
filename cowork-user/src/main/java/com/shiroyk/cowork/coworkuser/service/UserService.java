package com.shiroyk.cowork.coworkuser.service;

import com.google.common.collect.Streams;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkcommon.model.user.User;
import com.shiroyk.cowork.coworkuser.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public Optional<User> findUserByName(String name) {
        return userRepository.findUserByUsername(name);
    }

    public Long count() {
        return userRepository.count();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<UserDto> findUserDtoById(String id) {
        return userRepository.findById(id).map(User::toUserDtoL);
    }

    public List<User> findUsersByUsernameContains(String name) {
        return userRepository.findUsersByUsernameContains(name);
    }

    public Stream<User> findUserByIdList(List<String> users) {
        return Streams.stream(userRepository.findAllById(users));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

}
