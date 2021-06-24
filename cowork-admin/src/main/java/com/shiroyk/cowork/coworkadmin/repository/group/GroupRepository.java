package com.shiroyk.cowork.coworkadmin.repository.group;

import com.shiroyk.cowork.coworkcommon.model.group.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

    Optional<Group> findGroupByNameEquals(String name);

    Optional<Group> findGroupByLeader(String id);

    List<Group> findGroupsByNameContains(String name);

}
