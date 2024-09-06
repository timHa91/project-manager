package com.tim.projectmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.model.User;
import com.tim.projectmanagement.repository.UserRepository;
import com.tim.projectmanagement.request.CreateNewUserRequest;
import com.tim.projectmanagement.request.LoginRequest;
import com.tim.projectmanagement.service.EmailService;
import com.tim.projectmanagement.service.UserService;
import com.tim.projectmanagement.service.VerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static com.tim.projectmanagement.exception.ExceptionMessage.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private UserRepository<User> userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @MockBean
    private VerificationService verificationService;

    @MockBean
    private EmailService emailService;

    /** Register Tests **/
    @Test
    public void testRegisterUser_Success() throws Exception {
        CreateNewUserRequest user = new CreateNewUserRequest(
                "Max",
                "Mustermann",
                "test@example.com",
                "password",
                null
        );

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User successfully created"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    public void testRegisterUser_DuplicateEmail() throws Exception {
        CreateNewUserRequest user = new CreateNewUserRequest(
                "Max",
                "Mustermann",
                "duplicate@example.com",
                "password",
                null
        );

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value(EMAIL_ALREADY_IN_USE));
    }

    @Test
    public void testRegisterUser_InvalidEmail() throws Exception {
        CreateNewUserRequest user = new CreateNewUserRequest(
                "Max",
                "Mustermann",
                "invalid-email",
                "password",
                null
        );

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Invalid email."));
    }

    @Test
    public void testRegisterUser_FirstNameMissing() throws Exception {
        CreateNewUserRequest user = new CreateNewUserRequest(
                null,
                "Mustermann",
                "test@example.com",
                "password",
                null
        );

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("First Name is required."));
    }

    /** Login Tests **/
    @Test
    public void testLoginUser_Success() throws Exception {
        CreateNewUserRequest request = new CreateNewUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                null
        );

        when(verificationService.createVerificationUrl(any(), anyLong())).thenReturn("test-url");

        UserDTO createdUser = userService.createUser(request);

        User user = userRepository.findById(createdUser.getUserId());
        user.setEnabled(true);
        userRepository.update(user);

        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.refresh_token").exists());
    }

    @Test
    public void testLoginUser_UserDisabled() throws Exception {
        CreateNewUserRequest request = new CreateNewUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                null
        );

        when(verificationService.createVerificationUrl(any(), anyLong())).thenReturn("test-url");

        userService.createUser(request);

        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.reason").value("User is disabled"));
    }

    @Test
    public void testLoginUser_InvalidPassword() throws Exception {
        CreateNewUserRequest request = new CreateNewUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                null
        );

        when(verificationService.createVerificationUrl(any(), anyLong())).thenReturn("test-url");

        UserDTO createdUser = userService.createUser(request);

        User user = userRepository.findById(createdUser.getUserId());
        user.setEnabled(true);
        userRepository.update(user);

        LoginRequest loginRequest = new LoginRequest("john@example.com", "password12");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value(INCORRECT_EMAIL_OR_PASSWORD));
    }

    @Test
    public void testLoginUser_EmailNotExist() throws Exception {
        CreateNewUserRequest request = new CreateNewUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                null
        );

        UserDTO createdUser = userService.createUser(request);

        User user = userRepository.findById(createdUser.getUserId());
        user.setEnabled(true);
        userRepository.update(user);

        LoginRequest loginRequest = new LoginRequest("joh@example.com", "password123");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value(INCORRECT_EMAIL_OR_PASSWORD));
    }

    @Test
    public void testLoginUser_InvalidEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("johexample.com", "password123");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Invalid email. Please enter a valid email address"));
    }

    /** Reset Password Tests **/
    @Test
    public void resetPassword_Success() throws Exception {
        CreateNewUserRequest request = new CreateNewUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                null
        );

        when(verificationService.createVerificationUrl(any(), anyLong())).thenReturn("test-url");

        userService.createUser(request);

        mockMvc.perform(get("/user/resetpassword/{email}", request.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Password reset email sent\"}"));

    }

    @Test
    public void resetPassword_UserNotFound() throws Exception {
        String email = "nonexistent@example.com";

        mockMvc.perform(get("/user/resetpassword/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value(ERROR_RESETTING_PASSWORD));
    }

    @Test
    public void verifyPassword_Success() throws Exception {
        String token = "test";

        when(verificationService.createVerificationUrl(any(), anyLong())).thenReturn("test-url/test");


        mockMvc.perform(get("/verify/password/{token}", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value(ERROR_RESETTING_PASSWORD));
    }



}
