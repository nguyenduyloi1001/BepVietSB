package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.UserStatus;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.UserRequest;
import com.example.Bep_Viet.request.UserUpdateRequest;
import com.example.Bep_Viet.response.UserResponse;
import com.example.Bep_Viet.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .location(request.getLocation())
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        return mapToResponse(findUserById(userId));
    }

    @Override
    public List<UserResponse> getAllUser() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for (User user : users) {
            responses.add(mapToResponse(user));
        }
        return responses;
    }

    @Override
    public List<UserResponse> getAllUserByStatus(UserStatus status) {
        List<User> users = userRepository.findByStatus(status);
        List<UserResponse> responses = new ArrayList<>();
        for (User user : users) {
            responses.add(mapToResponse(user));
        }
        return responses;
    }

    @Override
    public UserResponse updateRoleUser(Long userId, Role role) {
        User user = findUserById(userId);
        user.setRole(role);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getAllUserByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        List<UserResponse> responses = new ArrayList<>();
        for (User user : users) {
            responses.add(mapToResponse(user));
        }
        return responses;
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToResponse(user);
    }

    @Transactional
    @Override
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateStatusUser(Long userId, UserStatus status) {
        User user = findUserById(userId);
        user.setStatus(status);
        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public void deleteUserById(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    // helper
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .location(user.getLocation())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }
}