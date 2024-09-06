package com.tim.projectmanagement.repository;

import com.tim.projectmanagement.model.User;

import java.util.Collection;

public interface UserRepository<T extends User> {

    T save(T data);
    Collection<T> list(int page, int pageSize);
    T findById(Long id);
    int update(T data);
    Boolean delete(Long id);

    Boolean existsByEmail(String email);
    T findByEmail(String email);
    void updatePassword(Long userId, String password);

    void enableUserAccount(Long userId);
}
