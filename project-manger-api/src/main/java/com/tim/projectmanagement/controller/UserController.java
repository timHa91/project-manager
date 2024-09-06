package com.tim.projectmanagement.controller;

import com.tim.projectmanagement.dto.UserDTO;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.jwt.JwtProvider;
import com.tim.projectmanagement.jwt.JwtUtils;
import com.tim.projectmanagement.mapper.UserMapper;
import com.tim.projectmanagement.model.HttpResponse;
import com.tim.projectmanagement.model.Role;
import com.tim.projectmanagement.model.UserPrincipal;
import com.tim.projectmanagement.request.CreateNewUserRequest;
import com.tim.projectmanagement.request.LoginRequest;
import com.tim.projectmanagement.request.NewPasswordRequest;
import com.tim.projectmanagement.request.UpdatePasswordRequest;
import com.tim.projectmanagement.service.RoleService;
import com.tim.projectmanagement.service.UserService;
import com.tim.projectmanagement.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static com.tim.projectmanagement.utils.HttpResponseUtil.createHttpResponseEntity;
import static com.tim.projectmanagement.utils.HttpResponseUtil.createHttpResponseEntityWithData;
import static com.tim.projectmanagement.utils.UserUtils.getAuthenticatedUser;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final VerificationService verificationService;
    private final JwtUtils jwtUtils;
    private final RoleService roleService;

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> registerUser(@RequestBody @Valid CreateNewUserRequest request) {
        UserDTO createdUser = userService.createUser(request);

        return createHttpResponseEntityWithData(
                CREATED,
                "User successfully created",
                Map.of("user", createdUser));
    }

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        userService.findUserEntityByEmail(loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return createHttpResponseEntityWithData(
                OK,
                "Login successful",
                Map.of(
                        "access_token", jwtProvider.generateAccessToken(userPrincipal),
                        "refresh_token", jwtProvider.generateRefreshToken(userPrincipal)
                ));
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> getUserProfile(Authentication authentication) {
        UserDTO authenticatedUser = userService.findUserByEmail(authentication.getName());

        return createHttpResponseEntityWithData(
                OK,
                "User Profile retrieved",
                Map.of("user", authenticatedUser));
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> initiatePasswordReset(@PathVariable("email") String email) {
        userService.resetPassword(email);

        return createHttpResponseEntity(
                OK,
                "Password reset email sent"
        );
    }

    @GetMapping("/verify/account/{token}")
    public ResponseEntity<HttpResponse> verifyUserAccount(@PathVariable("token") String token) {
        UserDTO user = verificationService.verifyAccount(token);

        return createHttpResponseEntity(
                OK,
                user.isEnabled() ? "Account already verified" : "Account successfully verified"
        );
    }

    @GetMapping("/verify/password/{token}")
    public ResponseEntity<HttpResponse> verifyPasswordResetToken(@PathVariable("token") String token) {
        UserDTO user = verificationService.verifyPasswordToken(token);

        return createHttpResponseEntityWithData(
                OK,
                "Please set a new password",
                Map.of("user", user)
        );
    }

    @PutMapping("/new/password")
    public ResponseEntity<HttpResponse> setNewPassword(@RequestBody @Valid NewPasswordRequest newPasswordForm) {
        userService.updatePassword(newPasswordForm.getUserId(), newPasswordForm.getPassword(), newPasswordForm.getConfirmPassword());

        return createHttpResponseEntity(
                OK,
                "Password successfully reset"
        );
    }

    @PutMapping("/update/password")
    public ResponseEntity<HttpResponse> changePassword(@RequestBody @Valid UpdatePasswordRequest newPasswordForm, Authentication authentication) {
        UserDTO user = Objects.requireNonNull(getAuthenticatedUser(authentication));
        userService.updatePassword(user.getUserId(), newPasswordForm.getNewPassword(), newPasswordForm.getConfirmNewPassword(), newPasswordForm.getCurrentPassword());

        return createHttpResponseEntity(
                OK,
                "Password successfully updated"
        );
    }

    @GetMapping("/refreshtoken")
    public ResponseEntity<HttpResponse> refreshAuthToken(HttpServletRequest request) {
        String token = jwtUtils.getTokenFromHeader(request);
        Long userId = jwtUtils.getSubjectFromToken(token, request);
        if(token == null || userId == null) {
            throw new ApiException("Invalid Refresh Token");
        }

        if (jwtUtils.isValidToken(userId, token)) {
            UserDTO user = userService.findUserById(userId);
            Role role = roleService.getRoleByUserId(userId);
            UserPrincipal userPrincipal = new UserPrincipal(UserMapper.toUser(user), role);
            String accessToken = jwtProvider.generateAccessToken(userPrincipal);

            return createHttpResponseEntityWithData(
                    OK,
                    "Access token refreshed",
                    Map.of(
                            "user", userPrincipal,
                            "access_token", accessToken,
                            "refresh_token", token
                    )
            );
        } else {
            return createHttpResponseEntity(
                    BAD_REQUEST,
                    "Refresh Token missing or invalid"
            );
        }
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason("There is no mapping for a " + request.getMethod() + " request for this path on the server")
                        .status(NOT_FOUND)
                        .statusCode(NOT_FOUND.value())
                        .build(), NOT_FOUND
        );
    }
}
