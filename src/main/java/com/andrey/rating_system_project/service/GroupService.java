package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.model.Group;
import com.andrey.rating_system_project.repository.GroupRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    public List<Group> findGroupsByFacultyId(Integer facultyId) {
        return groupRepository.findByFacultyId(facultyId);
    }
}