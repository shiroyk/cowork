package com.shiroyk.cowork.coworkgroup.repository;

import com.shiroyk.cowork.coworkcommon.model.group.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupRepository extends MongoRepository<Group, String> {
    List<Group> findGroupsByNameContains(String name);
}
