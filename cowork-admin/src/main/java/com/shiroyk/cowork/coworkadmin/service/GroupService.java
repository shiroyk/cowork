package com.shiroyk.cowork.coworkadmin.service;

import com.shiroyk.cowork.coworkadmin.repository.group.GroupRepository;
import com.shiroyk.cowork.coworkcommon.model.group.Group;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;

    public Optional<Group> findById(String id) {
        return groupRepository.findById(id);
    }

    public List<Group> findGroupsByNameContains(String name) {
        return groupRepository.findGroupsByNameContains(name);
    }

    public Page<Group> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    public Long count() {
        return groupRepository.count();
    }

    public Group save(Group group) {
        return groupRepository.save(group);
    }

    public void delete(String id) {
        groupRepository.deleteById(id);
    }
}
