package com.tim.projectmanagement.service;

import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.request.CreateNewUserRequest;

public interface UserService {
   UserDTO createUser(CreateNewUserRequest user);
   UserDTO findUserByEmail(String email);
   UserDTO findUserById(Long id);
   User findUserEntityByEmail(String email);
   void resetPassword(String email);
   void updatePassword(Long userId, String newPassword, String confirmPassword);
   void updatePassword(Long userId, String newPassword, String confirmPassword, String currentPassword);
}
