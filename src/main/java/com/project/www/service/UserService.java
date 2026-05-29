package com.project.www.service;

import com.project.www.dto.CreateUserRequest;
import com.project.www.dto.UserResponse;
import com.project.www.dto.ResetPasswordRequest;

import java.util.List;

public interface UserService {
    void createUser(CreateUserRequest request);
    List<UserResponse> getUsersByTenant(Long tenantId);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, CreateUserRequest request);
    void deleteUser(Long id);         // soft-deactivates, does not physically delete
    void deactivateUser(Long id);
    void resetPassword(Long id, ResetPasswordRequest request);
    List<com.project.www.dto.SupervisorResponse> getSupervisorsForRole(Long roleId, String roleCode);
}
