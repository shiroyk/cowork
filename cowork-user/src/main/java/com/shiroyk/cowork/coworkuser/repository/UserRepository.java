package com.shiroyk.cowork.coworkuser.repository;

import com.shiroyk.cowork.coworkcommon.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserByUsername(String name);

    List<User> findUsersByUsernameContains(String name);
}
