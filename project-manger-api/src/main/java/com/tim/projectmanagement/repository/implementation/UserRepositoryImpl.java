package com.tim.projectmanagement.repository.implementation;

import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.repository.UserRepository;
import com.tim.projectmanagement.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.tim.projectmanagement.query.UserQuery.*;
import static com.tim.projectmanagement.query.VerificationQuery.DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {
    private final NamedParameterJdbcTemplate jdbc;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User save(User user) {
        user.setEnabled(true);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource params = getSqlParameterSource(user);
        jdbc.update(INSERT_USER_QUERY, params, keyHolder);

        user.setUserId(requireNonNull(keyHolder.getKey()).longValue());
        user.setEnabled(false);
        user.setNotLocked(true);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    @Override
    public Collection<User> list(int page, int pageSize) {
        return List.of();
    }

    @Override
    public User findById(Long id) {
        return jdbc.queryForObject(SELECT_USER_BY_ID_QUERY, Map.of("userId", id), new UserRowMapper());
    }

    @Override
    public int update(@NotNull User user) {
        String updateQuery = "UPDATE Users SET first_name = :firstName, last_name = :lastName, email = :email, password = :password, enabled = :enabled WHERE user_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("enabled", user.isEnabled())
                .addValue("userId", user.getUserId());

        return jdbc.update(updateQuery, params);
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public Boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public User findByEmail(String email) {
        return jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
    }

    @Override
    public void updatePassword(Long userId, String password) {
        jdbc.update(UPDATE_USER_PASSWORD_BY_USER_ID_QUERY, of("userId", userId, "password",password));
        jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId", userId));
    }

    @Override
    public void enableUserAccount(Long userId) {
        jdbc.update(UPDATE_USER_ENABLED_QUERY, of("enabled", true, "userId", userId));
    }


    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }
}
