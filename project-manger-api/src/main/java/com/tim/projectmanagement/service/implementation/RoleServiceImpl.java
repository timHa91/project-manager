package com.tim.projectmanagement.service.implementation;

import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.exception.custom.DatabaseException;
import com.tim.projectmanagement.exception.custom.RoleNotFoundException;
import com.tim.projectmanagement.model.Role;
import com.tim.projectmanagement.repository.RoleRepository;
import com.tim.projectmanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import static com.tim.projectmanagement.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {
    private final RoleRepository<Role> roleRepository;

    @Override
    public void assignRoleToUser(String role, Long userId) {
        log.info("Assigning Role {} to User", role);
        try {
            roleRepository.assignRoleToUser(role, userId);
        }
        catch (EmptyResultDataAccessException e) {
                log.error("Role not found: {}", role, e);
                throw new RoleNotFoundException("No role found by name: " + role);
            } catch (DataAccessException e) {
                log.error("Database error while assigning role {} to user", role, e);
                throw new DatabaseException("Error assigning role to user");
            } catch (Exception e) {
                log.error("Unexpected error while assigning role {} to user", role, e);
                throw new ApiException("An unexpected error occurred while assigning role");
            }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        log.info("Getting Role from User");
        try {
            return roleRepository.getRoleByUserId(userId);
        } catch (EmptyResultDataAccessException e) {
            log.error("No role found for User", e);
            throw new ApiException(GENERAL_ERROR);
        } catch (Exception e) {
            log.error("Error getting role for user", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }
}
