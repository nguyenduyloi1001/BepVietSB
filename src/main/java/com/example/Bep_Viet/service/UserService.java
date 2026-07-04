package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.UserStatus;
import com.example.Bep_Viet.request.UserRequest;
import com.example.Bep_Viet.request.UserUpdateRequest;
import com.example.Bep_Viet.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long userId);
    List<UserResponse> getAllUser();
    List<UserResponse> getAllUserByStatus(UserStatus status);
    List<UserResponse> getAllUserByRole(Role role);
    UserResponse updateUser(Long userId, UserUpdateRequest request);
    UserResponse updateStatusUser(Long userId, UserStatus status);
    void deleteUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse updateRoleUser(Long userId, Role role);
}
