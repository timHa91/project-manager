package com.tim.projectmanagement.repository.implementation;

import com.tim.projectmanagement.model.Role;
import com.tim.projectmanagement.repository.RoleRepository;
import com.tim.projectmanagement.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.tim.projectmanagement.query.RoleQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository<Role> {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Role save(Role data) {
        return null;
    }

    @Override
    public Collection list() {
        return List.of();
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public void assignRoleToUser(String roleName, Long userId) {
        Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName", roleName), new RoleRowMapper());
        jdbc.update(INSERT_USER_ROLE_QUERY, Map.of("userId", userId, "roleId", Objects.requireNonNull(role).getRoleId()));
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        return jdbc.queryForObject(SELECT_ROLE_BY_ID_QUERY, Map.of("userId", userId), new RoleRowMapper());
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
