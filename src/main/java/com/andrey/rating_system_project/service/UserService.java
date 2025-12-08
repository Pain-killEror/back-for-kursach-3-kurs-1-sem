package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.UserCreateDto;
import com.andrey.rating_system_project.dto.UserUpdateDto;
import com.andrey.rating_system_project.exception.ResourceNotFoundException;
import com.andrey.rating_system_project.model.Faculty;
import com.andrey.rating_system_project.model.Group;
import com.andrey.rating_system_project.model.Role;
import com.andrey.rating_system_project.model.StudentInfo;
import com.andrey.rating_system_project.model.Subject;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.model.enums.EducationForm;
import com.andrey.rating_system_project.model.enums.UserStatus;
import com.andrey.rating_system_project.repository.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupRepository groupRepository;
    private final FacultyRepository facultyRepository;
    private final StudentInfoRepository studentInfoRepository;
    // Добавляем репозиторий предметов
    private final SubjectRepository subjectRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       GroupRepository groupRepository, FacultyRepository facultyRepository,
                       StudentInfoRepository studentInfoRepository, SubjectRepository subjectRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.groupRepository = groupRepository;
        this.facultyRepository = facultyRepository;
        this.studentInfoRepository = studentInfoRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + login));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByLogin(username);

        if (user.getRole() == null) {
            throw new UsernameNotFoundException("User has not been approved yet");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getName()))
        );
    }

    @Transactional
    public User registerUser(UserCreateDto userDto) {
        if (userRepository.existsByLogin(userDto.getLogin())) {
            throw new IllegalArgumentException("User with this login already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        User user = new User();
        user.setLogin(userDto.getLogin());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setRole(null);
        user.setStatus(UserStatus.PENDING);

        return userRepository.save(user);
    }

    @Transactional
    public User approveUser(Integer userId, Integer roleId) {
        User user = findUserById(userId);

        if (user.getStatus() != UserStatus.PENDING) {
            // Можно просто проигнорировать, если пользователь уже подтвержден
            // throw new IllegalStateException("User is not in PENDING status.");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        if (role.getName().equals("STUDENT")) {
            if (user.getStudentInfo() == null) {
                StudentInfo studentInfo = new StudentInfo();
                studentInfo.setUser(user);
                studentInfo.setEducationForm(EducationForm.BUDGET); // По умолчанию
                user.setStudentInfo(studentInfo);
            }
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Integer userId, UserUpdateDto updateDto) {
        User user = findUserById(userId);

        if (updateDto.getFullName() != null) {
            user.setFullName(updateDto.getFullName());
        }

        if (updateDto.getEmail() != null) {
            user.setEmail(updateDto.getEmail());
        }

        // Обновление факультета
        if (updateDto.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(updateDto.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
            user.setFaculty(faculty);
        }

        // Обновление группы (для студентов)
        if (updateDto.getGroupId() != null) {
            if (user.getRole() != null && user.getRole().getName().equals("STUDENT")) {
                Group group = groupRepository.findById(updateDto.getGroupId())
                        .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

                if (user.getStudentInfo() == null) {
                    StudentInfo info = new StudentInfo();
                    info.setUser(user);
                    info.setEducationForm(EducationForm.BUDGET);
                    user.setStudentInfo(info);
                }
                user.getStudentInfo().setGroup(group);
            }
        }

        // НОВАЯ ЛОГИКА: Обновление предметов (для преподавателей)
        if (updateDto.getSubjectIds() != null) {
            if (user.getRole() != null && user.getRole().getName().equals("TEACHER")) {
                List<Subject> subjects = subjectRepository.findAllById(updateDto.getSubjectIds());
                // Set, так как отношение ManyToMany
                user.setSubjects(new HashSet<>(subjects));
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public StudentInfo assignStudentToGroup(Integer studentId, Integer groupId) {
        User user = findUserById(studentId);
        if (user.getRole() == null || !user.getRole().getName().equals("STUDENT")) {
            throw new IllegalStateException("User is not a student.");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (user.getStudentInfo() == null) {
            throw new ResourceNotFoundException("Student info not found for user id: " + studentId);
        }

        user.getStudentInfo().setGroup(group);
        userRepository.save(user);

        return user.getStudentInfo();
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}