package com.tim.projectmanagement.service.implementation;


import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.exception.custom.InvalidLinkException;
import com.tim.projectmanagement.mapper.UserMapper;
import com.tim.projectmanagement.enumeration.VerificationType;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.exception.custom.LinkExpiredException;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.repository.VerificationRepository;
import com.tim.projectmanagement.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.tim.projectmanagement.enumeration.VerificationType.ACCOUNT;
import static com.tim.projectmanagement.enumeration.VerificationType.PASSWORD;
import static com.tim.projectmanagement.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {
    private final VerificationRepository<User> verificationRepository;

    @Override
    public String createVerificationUrl(VerificationType type, Long userId) {
        log.info("Creating {} Verification URL for user", type.getType());
        if (userId == null) {
            log.error("User Id is null");
            throw new IllegalArgumentException(GENERAL_ERROR);
        }

        String token = UUID.randomUUID().toString();
        String url = getVerificationUrl(token, type.getType());

        try {
            if (type == ACCOUNT) {
                return verificationRepository.setAccountVerificationUrl(url, userId, calculateExpirationDate());
            } else {
                return verificationRepository.setResetPasswordUrl(userId, url, calculateExpirationDate());
            }

        } catch (Exception e) {
            log.error("Error creating verification URL for user", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    @Override
    public UserDTO verifyPasswordToken(String token) {
        String url = getVerificationUrl(token, PASSWORD.getType());

        if (isLinkExpired(url)) {
            throw new LinkExpiredException(LINK_EXPIRED);
        }
        try {
            User user = verificationRepository.getUserByVerifyUrl(url, PASSWORD);
            return UserMapper.toUserDTO(user);
        } catch (EmptyResultDataAccessException exception) {
            log.error("Invalid link", exception);
            throw new ApiException(PASSWORD_LINK_NOT_VALID);
        } catch (Exception e) {
            log.error("Error verifying password token", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    @Override
    public UserDTO verifyAccount(String token) {
        try {
            String url = getVerificationUrl(token, ACCOUNT.getType());
            User user = verificationRepository.verifyAccount(url);

            return UserMapper.toUserDTO(user);
        }catch (EmptyResultDataAccessException e) {
            log.error("No User Entry for the URL", e);
            throw new InvalidLinkException(ACCOUNT_VERIFY_URL_ALREADY_USED);

        } catch (Exception e) {
            log.error("Error while verifying Account", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    private String getVerificationUrl(String token, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + token).toUriString();
    }

    public Boolean isLinkExpired(String url) {
        try {
            return verificationRepository.getResetPasswordLinkExpirationDate(url);
        } catch (EmptyResultDataAccessException exception) {
            log.error("Invalid link", exception);
            throw new ApiException(LINK_EXPIRED);
        } catch (Exception e) {
            log.error("Error checking link expiration", e);
            throw new ApiException(GENERAL_ERROR);
        }
    }

    private LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plusDays(1);
    }
}
