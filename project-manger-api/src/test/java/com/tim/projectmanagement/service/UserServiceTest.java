package com.tim.projectmanagement.service;

import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.enumeration.RoleType;
import com.tim.projectmanagement.enumeration.VerificationType;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.exception.custom.EmailAlreadyExistsException;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.repository.UserRepository;
import com.tim.projectmanagement.repository.VerificationRepository;
import com.tim.projectmanagement.request.CreateNewUserRequest;
import com.tim.projectmanagement.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository<User> userRepository;
    @Mock
    private VerificationService verificationService;
    @Mock
    private RoleService roleService;
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationRepository<User> verificationRepository;
    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User expectedUser;
    private CreateNewUserRequest testUser;

    @BeforeEach
    void setUp() {
        expectedUser = User.builder()
                .userId(1L)
                .firstName("Max")
                .lastName("Mustermann")
                .email("test@example.com")
                .password("password")
                .build();

        testUser = new CreateNewUserRequest(
                "Max",
                "Mustermann",
                "test@example.com",
                "password",
                null
        );
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(verificationService.createVerificationUrl(VerificationType.ACCOUNT, expectedUser.getUserId())).thenReturn("http://verify.com");


        UserDTO result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(roleService).assignRoleToUser(eq(RoleType.ROLE_USER.name()), eq(1L));
        verify(emailService).sendVerificationEmail(eq("Max"), eq("test@example.com"), eq("http://verify.com"), eq(VerificationType.ACCOUNT));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(testUser));
    }

    @Test
    void createUser_GeneralError() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(ApiException.class, () -> userService.createUser(testUser));
    }
}
