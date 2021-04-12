package com.shiroyk.cowork.coworkgroup.repository;

import com.shiroyk.cowork.coworkgroup.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends MongoRepository<Group, String> {

    Optional<Group> findGroupByNameEquals(String name);

    Optional<Group> findGroupByLeader(String id);

    List<Group> findGroupsByNameContains(String name);

}
