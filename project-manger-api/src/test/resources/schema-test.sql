USE projectmanagement_test;

-- Drop existing tables in reverse order of their dependencies
DROP TABLE IF EXISTS ResetPasswordVerifications;
DROP TABLE IF EXISTS AccountVerifications;
DROP TABLE IF EXISTS UserEvents;
DROP TABLE IF EXISTS Events;
DROP TABLE IF EXISTS UserRoles;
DROP TABLE IF EXISTS Users;

-- Recreate tables
CREATE TABLE Users (
                       user_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       image_url VARCHAR(255) DEFAULT NULL,
                       enabled BOOLEAN DEFAULT FALSE,
                       is_not_locked BOOLEAN DEFAULT TRUE,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT UQ_Users_Email UNIQUE (email)
);


CREATE TABLE UserRoles (
                           user_role_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           role_id BIGINT NOT NULL,
                           FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
                           FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE RESTRICT ON UPDATE CASCADE,
                           CONSTRAINT UQ_UserRoles_User_Id UNIQUE (user_id)
);

CREATE TABLE Events (
                        event_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                        type VARCHAR(50) NOT NULL CHECK(type IN ('LOGIN_ATTEMPT', 'LOGIN_ATTEMPT_FAILURE', 'LOGIN_ATTEMPT_SUCCESS', 'PROFILE_UPDATE', 'PROFILE_PICTURE_UPDATE', 'ROLE_UPDATE', 'ACCOUNT_SETTINGS_UPDATE', 'PASSWORD_UPDATE', 'MFA_UPDATE')),
                        description VARCHAR(255) NOT NULL,
                        CONSTRAINT UQ_Events_Type UNIQUE (type)
);

CREATE TABLE UserEvents (
                            user_event_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            event_id BIGINT NOT NULL,
                            device VARCHAR(100) DEFAULT NULL,
                            ip_address VARCHAR(100) DEFAULT NULL,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE RESTRICT ON UPDATE CASCADE,
                            FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE AccountVerifications (
                                      auth_url_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                      user_id BIGINT NOT NULL,
                                      url VARCHAR(255) NOT NULL,
                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      expired_at DATETIME NOT NULL,
                                      FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
                                      CONSTRAINT UQ_AccountVerifications_User_Id UNIQUE (user_id),
                                      CONSTRAINT UQ_AccountVerifications_Url UNIQUE (url)
);

CREATE TABLE ResetPasswordVerifications (
                                            reset_pw_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                            user_id BIGINT NOT NULL,
                                            url VARCHAR(255) NOT NULL,
                                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                            expired_at DATETIME NOT NULL,
                                            FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
                                            CONSTRAINT UQ_ResetPasswordVerification_User_Id UNIQUE (user_id),
                                            CONSTRAINT UQ_ResetPasswordVerification_Url UNIQUE (url)
);

-- Insert base roles
# INSERT INTO Roles (name, permission) VALUES
#                                          ('ROLE_USER', 'READ:USER,READ:CUSTOMER'),
#                                          ('ROLE_MANAGER', 'READ:USER,READ:CUSTOMER,UPDATE:USER,UPDATE:CUSTOMER'),
#                                          ('ROLE_ADMIN', 'READ:USER,READ:CUSTOMER,UPDATE:USER,UPDATE:CUSTOMER,CREATE:USER,CREATE:CUSTOMER'),
#                                          ('ROLE_SYSADMIN', 'READ:USER,READ:CUSTOMER,UPDATE:USER,UPDATE:CUSTOMER,CREATE:USER,CREATE:CUSTOMER,DELETE:USER,DELETE:CUSTOMER');
