package com.tim.projectmanagement.query;

public class UserQuery {
    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM Users WHERE email = :email";
    public static final String SELECT_USER_BY_EMAIL_QUERY = "SELECT * FROM Users WHERE email = :email";
    public static final String INSERT_USER_QUERY = "INSERT INTO Users(email, password, first_name, last_name) VALUES(:email, :password, :firstName, :lastName)";
    public static final String SELECT_USER_BY_ID_QUERY = "SELECT * FROM Users WHERE user_id = :userId";
    public static final String SELECT_USER_BY_RESET_PASSWORD_URL_QUERY = "SELECT * FROM Users WHERE user_id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :verificationUrl)";
    public static final String UPDATE_USER_PASSWORD_BY_USER_ID_QUERY = "UPDATE Users SET password = :password WHERE user_id = :userId";
    public static final String UPDATE_USER_ENABLED_QUERY = "UPDATE Users SET enabled = :enabled WHERE user_id = :userId";

}
