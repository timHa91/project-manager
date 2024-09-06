package com.tim.projectmanagement.mapper;

import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.model.Role;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.request.CreateNewUserRequest;
import org.springframework.beans.BeanUtils;

public class UserMapper {
    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    public static UserDTO toUserDTO(User user, Role role) {
        if (user == null || role == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setRoleName(role.getName());
        userDTO.setPermissions(role.getPermission());

        return userDTO;
    }

    public static User toUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        return user;
    }

    public static User toUser(CreateNewUserRequest userRequest) {
        if (userRequest == null) {
            return null;
        }

        User user = new User();
        BeanUtils.copyProperties(userRequest, user);

        return user;
    }
}
