package com.tim.projectmanagement.service.implementation;

import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.exception.custom.EmailAlreadyExistsException;
import com.tim.projectmanagement.exception.custom.PasswordException;
import com.tim.projectmanagement.model.Role;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.repository.UserRepository;
import com.tim.projectmanagement.request.CreateNewUserRequest;
import com.tim.projectmanagement.service.EmailService;
import com.tim.projectmanagement.service.RoleService;
import com.tim.projectmanagement.service.UserService;
import com.tim.projectmanagement.service.VerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.tim.projectmanagement.enumeration.RoleType.ROLE_USER;
import static com.tim.projectmanagement.enumeration.VerificationType.ACCOUNT;
import static com.tim.projectmanagement.enumeration.VerificationType.PASSWORD;
import static com.tim.projectmanagement.exception.ExceptionMessage.*;
import static com.tim.projectmanagement.mapper.UserMapper.toUser;
import static com.tim.projectmanagement.mapper.UserMapper.toUserDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;
    private final VerificationService verificationService;
    private final RoleService roleService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public UserDTO createUser(CreateNewUserRequest user) {
        log.info("Creating user");

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(EMAIL_ALREADY_IN_USE);
        }

        try {
            User savedUser = userRepository.save(toUser(user));
            roleService.assignRoleToUser(ROLE_USER.name(), savedUser.getUserId());
            String verificationUrl = verificationService.createVerificationUrl(ACCOUNT, savedUser.getUserId());
            emailService.sendVerificationEmail(user.getFirstName(), savedUser.getEmail(), verificationUrl, ACCOUNT);

            return toUserDTO(savedUser);

        } catch (EmptyResultDataAccessException e) {
            log.error("Database error while creating user", e);
            throw new ApiException(GENERAL_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error while creating user", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    @Override
    public UserDTO findUserByEmail(String email) {
        try {
            User user = findUserEntityByEmail(email);
            Role role = roleService.getRoleByUserId(user.getUserId());

            return toUserDTO(user, role);
        } catch (EmptyResultDataAccessException e) {
            log.error("User not found by email", e);
            throw new ApiException(GENERAL_ERROR);
        } catch (Exception e) {
            log.error("Error finding user by email", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    @Override
    public User findUserEntityByEmail(String email) {
        log.info("Trying to fetch user by email");
        try {
            return userRepository.findByEmail(email);
        } catch (EmptyResultDataAccessException e) {
            log.error("User not found by email", e);
            throw new UsernameNotFoundException(INCORRECT_EMAIL_OR_PASSWORD);
        } catch (Exception e) {
            log.error("Error finding user by email", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    @Override
    public void resetPassword(String email) {
        try {
            User user = userRepository.findByEmail(email);
            String verificationUrl = verificationService.createVerificationUrl(PASSWORD, user.getUserId());
            emailService.sendVerificationEmail(user.getFirstName(), email, verificationUrl, PASSWORD);
        } catch (EmptyResultDataAccessException e) {
            log.error("User not found by email", e);
            throw new UsernameNotFoundException(ERROR_RESETTING_PASSWORD);
        } catch (Exception e) {
            log.error("Error resetting password for email", e);
            throw new PasswordException(ERROR_RESETTING_PASSWORD);
        }
    }

    @Override
    public void updatePassword(Long userId, String newPassword, String confirmPassword) {
        if (!newPassword.equalsIgnoreCase(confirmPassword)) {
            throw new PasswordException(ENTERED_PASSWORDS_DO_NOT_MATCH);
        }
        try {
            userRepository.updatePassword(userId, encoder.encode(newPassword));
        } catch (EmptyResultDataAccessException e) {
            log.error("User not found by ID", e);
            throw new PasswordException(ERROR_UPDATING_PASSWORD);
        } catch (Exception e) {
            log.error("Error updating password for user ID", e);
            throw new PasswordException(ERROR_UPDATING_PASSWORD);
        }
    }

    @Override
    public void updatePassword(Long userId, String newPassword, String confirmPassword, String currentPassword) {
        if (!newPassword.equalsIgnoreCase(confirmPassword)) {
            throw new PasswordException(ENTERED_PASSWORDS_DO_NOT_MATCH);
        }
        try {
            User user = userRepository.findById(userId);
            if (!encoder.matches(currentPassword, user.getPassword())) {
                throw new ApiException(CURRENT_PASSWORD_INCORRECT);
            }
            userRepository.updatePassword(userId, encoder.encode(newPassword));
        } catch (EmptyResultDataAccessException e) {
            log.error("User not found by ID", e);
            throw new PasswordException(ERROR_UPDATING_PASSWORD );
        } catch (PasswordException e) {
            log.error("Password reset error for user ID", e);
            throw new PasswordException(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating password for user ID", e);
            throw new ApiException(ERROR_UPDATING_PASSWORD);
        }
    }

    @Override
    public UserDTO findUserById(Long id) {
        log.info("Getting user by ID");
        try {
            User user = userRepository.findById(id);
            Role role = roleService.getRoleByUserId(user.getUserId());

            return toUserDTO(user, role);
        } catch (EmptyResultDataAccessException e) {
            log.error("User not found by ID", e);
            throw new ApiException(GENERAL_ERROR);
        } catch (Exception e) {
            log.error("Error finding user by ID", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }
}
