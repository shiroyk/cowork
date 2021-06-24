package com.shiroyk.cowork.coworkadmin.service;

import com.google.common.collect.Streams;
import com.shiroyk.cowork.coworkadmin.repository.user.UserRepository;
import com.shiroyk.cowork.coworkcommon.model.user.User;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<User> findAllUser(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Stream<User> findUserByIdList(Iterable<String> users) {
        return Streams.stream(userRepository.findAllById(users));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void saveAll(List<User> users) {
        userRepository.saveAll(users);
    }
}
