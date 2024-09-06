package com.tim.projectmanagement.service;

import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.enumeration.VerificationType;

public interface VerificationService {
    String createVerificationUrl(VerificationType type, Long userId);
    UserDTO verifyPasswordToken(String token);

    UserDTO verifyAccount(String token);
}
