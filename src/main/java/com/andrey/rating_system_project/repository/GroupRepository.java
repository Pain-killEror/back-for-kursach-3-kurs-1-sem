package com.andrey.rating_system_project.repository;


import com.andrey.rating_system_project.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    List<Group> findByFacultyId(Integer facultyId);

}
