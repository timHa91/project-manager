package com.tim.projectmanagement.service;

import com.tim.projectmanagement.enumeration.RoleType;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.exception.custom.DatabaseException;
import com.tim.projectmanagement.exception.custom.RoleNotFoundException;
import com.tim.projectmanagement.model.Role;
import com.tim.projectmanagement.repository.RoleRepository;
import com.tim.projectmanagement.service.implementation.RoleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository<Role> roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void assignRoleToUser_Success() {
        RoleType role = RoleType.ROLE_USER;
        Long userId = 1L;

        doNothing().when(roleRepository).assignRoleToUser(anyString(), anyLong());

        assertDoesNotThrow(()-> roleService.assignRoleToUser(role.name(), userId));

        verify(roleRepository).assignRoleToUser(role.name(), userId);
    }

    @Test
    void assignRoleToUser_DatabaseError() {
        String role = "Moderator";
        Long userId = 1L;

        doThrow(new DatabaseException("Database error") {}).when(roleRepository).assignRoleToUser(anyString(), anyLong());

        assertThrows(DatabaseException.class, () -> roleService.assignRoleToUser(role, userId));
    }

    @Test
    void assignRoleToUser_RoleNotFond() {
        RoleType role = RoleType.ROLE_ADMIN;
        Long userId = 1L;

        doThrow(new EmptyResultDataAccessException(1)).when(roleRepository).assignRoleToUser(anyString(), anyLong());
        assertThrows(RoleNotFoundException.class, () -> roleService.assignRoleToUser(role.name(), userId));
    }

    @Test
    void assignRoleToUser_UnexpectedError() {
        RoleType role = RoleType.ROLE_USER;
        Long userId = 1L;

        doThrow(new RuntimeException("Unexpected error")).when(roleRepository).assignRoleToUser(anyString(), anyLong());

        assertThrows(ApiException.class, () -> roleService.assignRoleToUser(role.name(), userId));
    }
}
