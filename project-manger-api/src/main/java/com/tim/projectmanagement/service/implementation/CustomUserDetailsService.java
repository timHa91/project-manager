package com.tim.projectmanagement.service.implementation;

import com.tim.projectmanagement.model.Role;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.model.UserPrincipal;
import com.tim.projectmanagement.service.RoleService;
import com.tim.projectmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.tim.projectmanagement.exception.ExceptionMessage.INCORRECT_EMAIL_OR_PASSWORD;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userService.findUserEntityByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException(INCORRECT_EMAIL_OR_PASSWORD);
            }
            Role role = roleService.getRoleByUserId(user.getUserId());

            return new UserPrincipal(user, role);
        } catch (Exception e) {
            log.error("Unexpected error occurred while loading user by email", e);
            throw new UsernameNotFoundException(INCORRECT_EMAIL_OR_PASSWORD, e);
        }
    }
}
