package com.shiroyk.cowork.coworkuser.repository;

import com.shiroyk.cowork.coworkuser.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserByUsername(String name);

    Optional<User> findUserByEmail(String email);

    User findUserById(String id);

    List<User> findUsersByUsernameContains(String name);

    List<User> findUsersByGroup(String group, Pageable pageable);
}
