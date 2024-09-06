package com.tim.projectmanagement.service.implementation;

import com.tim.projectmanagement.enumeration.VerificationType;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.tim.projectmanagement.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Override
    @Async
    public void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String text = constructEmail(firstName, verificationUrl, verificationType);
            message.setFrom("tim.andre.hartmann@gmail.com");
            message.setText(text);
            message.setTo(email);
            message.setSubject(String.format("SecureCapita - %s Verification Email", StringUtils.capitalize(verificationType.getType())));
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending verification email", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    private String constructEmail(String firstName, String verificationUrl, VerificationType verificationType) {
        switch (verificationType) {
            case PASSWORD -> {
                return "Hello " + firstName + "," + "\n\nReset password request. Please click the link below to reset your password. \n\n" + verificationUrl + "\n\nThe Support Team";
            }
            case ACCOUNT -> {
                return "Hello " + firstName + "," + "\n\nYour new account has been created. Please click the link below to verify your account. \n\n" + verificationUrl + "\n\nThe Support Team";
            }
            default -> throw new ApiException(GENERAL_ERROR);
        }
    }
}
