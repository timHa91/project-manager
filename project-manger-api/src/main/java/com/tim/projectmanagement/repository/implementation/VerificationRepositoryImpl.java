package com.tim.projectmanagement.repository.implementation;

import com.tim.projectmanagement.enumeration.VerificationType;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.repository.UserRepository;
import com.tim.projectmanagement.repository.VerificationRepository;
import com.tim.projectmanagement.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static com.tim.projectmanagement.exception.ExceptionMessage.GENERAL_ERROR;
import static com.tim.projectmanagement.query.UserQuery.SELECT_USER_BY_RESET_PASSWORD_URL_QUERY;
import static com.tim.projectmanagement.query.VerificationQuery.*;
import static java.util.Map.of;

@Repository
@RequiredArgsConstructor
public class VerificationRepositoryImpl implements VerificationRepository<User> {
    private final NamedParameterJdbcTemplate jdbc;
    private final UserRepository<User> userRepository;

    @Override
    public String setAccountVerificationUrl(String verificationUrl, Long userId, LocalDateTime expiredAt) {
        jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("url", verificationUrl, "userId", userId, "expiredAt", expiredAt));

        return verificationUrl;
    }

    @Override
    public String setResetPasswordUrl(Long userId, String verificationUrl, LocalDateTime expirationDate) {
        jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId",  userId));
        jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY, of("userId", userId, "url", verificationUrl, "expirationDate", expirationDate));

        return verificationUrl;
    }

    @Override
    public User getUserByVerifyUrl(String verificationUrl, VerificationType verificationType) {
        switch (verificationType) {
            case PASSWORD -> {
                return jdbc.queryForObject(SELECT_USER_BY_RESET_PASSWORD_URL_QUERY, Map.of("verificationUrl", verificationUrl), new UserRowMapper());
            }
            case ACCOUNT -> {
                return jdbc.queryForObject(SELECT_USER_BY_RESET_PASSWORD_URL_QUERY, Map.of("verificationrl", verificationUrl), new UserRowMapper());
            }
            default -> {
                throw new ApiException(GENERAL_ERROR);
            }
        }
    }

    @Override
    public Boolean getResetPasswordLinkExpirationDate(String verificationUrl) {
        return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL, Map.of("verificationUrl", verificationUrl), Boolean.class);
    }

    @Override
    public User verifyAccount(String verificationUrl) {
        User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY, of("verificationUrl", verificationUrl), new UserRowMapper());
        userRepository.enableUserAccount(Objects.requireNonNull(user).getUserId());
        jdbc.update(DELETE_ACCOUNT_VERIFICATION_BY_USER_ID_QUERY, of("userId",  user.getUserId()));

        return user;
    }
}
