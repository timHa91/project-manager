package com.tim.projectmanagement.service;

import com.tim.projectmanagement.enumeration.VerificationType;

public interface EmailService {
    void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType type);
}
