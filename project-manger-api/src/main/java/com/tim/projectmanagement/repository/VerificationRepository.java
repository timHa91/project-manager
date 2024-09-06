package com.tim.projectmanagement.repository;

import com.tim.projectmanagement.enumeration.VerificationType;
import com.tim.projectmanagement.model.User;

import java.time.LocalDateTime;

public interface VerificationRepository<T extends User> {
    String setAccountVerificationUrl(String verificationUrl, Long userId, LocalDateTime expiredAt);

    String setResetPasswordUrl(Long userId, String url, LocalDateTime expirationDate);

    T getUserByVerifyUrl(String url, VerificationType verificationType);
    Boolean getResetPasswordLinkExpirationDate(String verificationUrl);

    T verifyAccount(String verificationUrl);
}
