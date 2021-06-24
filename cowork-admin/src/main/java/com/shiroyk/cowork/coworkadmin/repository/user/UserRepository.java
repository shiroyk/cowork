package com.shiroyk.cowork.coworkadmin.repository.user;

import com.shiroyk.cowork.coworkcommon.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserByUsername(String name);
}
