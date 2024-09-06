package com.tim.projectmanagement.query;

public class VerificationQuery {
    public static final String DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY = "DELETE FROM ResetPasswordVerifications WHERE user_id = :userId";
    public static final String INSERT_PASSWORD_VERIFICATION_QUERY = "INSERT INTO ResetPasswordVerifications (user_id, url, expired_at) VALUES (:userId, :url, :expirationDate)";
    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO AccountVerifications(url, user_id, expired_at) VALUES (:url, :userId, :expiredAt)";
    public static final String SELECT_EXPIRATION_BY_URL = "SELECT expired_at < NOW() AS is_expired FROM ResetPasswordVerifications WHERE url = :verificationUrl ";
    public static final String SELECT_USER_BY_ACCOUNT_URL_QUERY = "SELECT * FROM Users WHERE user_id = (SELECT user_id FROM AccountVerifications WHERE url = :verificationUrl)";
    public static final String DELETE_ACCOUNT_VERIFICATION_BY_USER_ID_QUERY = "DELETE FROM AccountVerifications WHERE user_id = :userId";
}
