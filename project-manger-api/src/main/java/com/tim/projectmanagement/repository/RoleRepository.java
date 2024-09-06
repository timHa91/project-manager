package com.tim.projectmanagement.repository;

import com.tim.projectmanagement.model.Role;

import java.util.Collection;

public interface RoleRepository <T extends Role> {
    /* CRUD Operations */
    T save(T data);
    Collection<T> list();
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    /* Complex Queries */
    void assignRoleToUser(String roleName, Long userId);
    T getRoleByUserId(Long userId);
    T getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);
}
