package com.tim.projectmanagement.service;

import com.tim.projectmanagement.enumeration.VerificationType;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.service.implementation.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static com.tim.projectmanagement.exception.ExceptionMessage.GENERAL_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private String firstName;
    private String email;
    private String verificationUrl;
    private VerificationType verificationType;

    @BeforeEach
    void setUp() {
        firstName = "John";
        email = "john.doe@example.com";
        verificationUrl = "http://example.com/verify";
        verificationType = VerificationType.ACCOUNT;
    }

    @Test
    void sendVerificationEmail_shouldSendEmailSuccessfully() {
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        emailService.sendVerificationEmail(firstName, email, verificationUrl, verificationType);

        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationEmail_shouldThrowApiException_whenEmailSendingFails() {
        doThrow(new RuntimeException("Email sending failed")).when(emailSender).send(any(SimpleMailMessage.class));

        ApiException exception = assertThrows(ApiException.class, () -> {
            emailService.sendVerificationEmail(firstName, email, verificationUrl, verificationType);
        });

        assertEquals(GENERAL_ERROR, exception.getMessage());

        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
