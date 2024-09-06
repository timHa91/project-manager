package com.tim.projectmanagement.utils;

import com.tim.projectmanagement.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@Slf4j
public class UserUtils {

    public static UserDTO getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            log.warn("No authentication object found in security context");
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDTO) {
            return (UserDTO) principal;
        } else {
            log.warn("Principal is not an instance of UserDTO: {}", principal);
            return null;
        }
    }
}
