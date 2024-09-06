package com.tim.projectmanagement.service;

import com.tim.projectmanagement.enumeration.RoleType;
import com.tim.projectmanagement.model.Role;

public interface RoleService {
    void assignRoleToUser(String role, Long userId);
    Role getRoleByUserId(Long userId);
}
